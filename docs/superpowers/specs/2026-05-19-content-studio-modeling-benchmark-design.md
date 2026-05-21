# Content Studio Modeling Benchmark Design

> Working spec for review only. This file is implementation scaffolding and must be deleted before the final master-ready result.

## Goal

Rebuild `cap4k-reference-content-studio` into a clean cap4k modeling benchmark.

The project should remain small and readable, but it should demonstrate a complete business flow:

- content creation
- aggregate state transitions
- domain event publication
- domain event listeners
- external capability clients
- inbound integration event consumption
- outbound integration event publication
- job fallback
- Saga orchestration
- domain service decision
- repository, factory, and UoW usage
- HTTP adapter layering
- command and query routes
- code generation ownership
- code analysis and causal flow output
- value object modeling

This is a reference project, not a production platform. Readability is a first-class requirement.

## Non-Goals

- Do not add authentication, authorization, frontend UI, production deployment, observability, or real external provider integration.
- Do not model `Specification` into the main flow just to show the generated artifact.
- Do not commit this spec or any implementation plan to the final master-ready branch.
- Do not make tests look like internal construction scaffolding. Tests are part of the public reading surface.

## Positioning

Use a mixed benchmark shape:

- The default immediate publication path demonstrates the conservative cap4k default route: aggregate, command, query, domain event, subscriber, external capability client, inbound external fact, job fallback, repository, factory, and UoW.
- The paid publication path is an explicit advanced route. It demonstrates Saga, compensation, external paid capabilities, domain service, richer process state, and operator repair.
- `cap4kAnalysisGenerate` output is evidence, not a modeling concept. It should show both default and advanced flows because those real code paths exist.

## Business Vocabulary

- `Content`: authored material moving from draft to published.
- `Review`: local approval fact required before publication.
- `Media processing`: external work that converts a media source into a publishable asset.
- `Media ready`: local accepted fact that required media processing succeeded.
- `Release policy`: publication route, either immediate or paid.
- `Paid publication`: advanced process that coordinates payout hold, entitlement plan, content publication, activation, compensation, and repair.
- `Payout hold`: external creator settlement reservation.
- `Access entitlement plan`: external access grant plan for paid content.
- `Operator repair`: terminal state when automated compensation cannot safely finish the paid process.

## Bounded Context

The bounded context is `Content Studio`.

It owns:

- content lifecycle facts
- review facts
- local media readiness facts
- local paid publication process facts

It does not own:

- the real media processing service
- the real payout service
- the real entitlement service
- external event transport delivery guarantees

External systems are represented through capability clients, inbound integration events, outbound integration events, and adapter handlers.

## Aggregates

### Content

`Content` owns the content lifecycle and publication eligibility facts.

Fields:

- `title`
- `body`
- `mediaSourceKey`
- `reviewStatus`
- `contentStatus`
- `releasePolicy`
- `reviewerId`
- `reviewedAt`
- `mediaReadyAt`
- `publishedAt`

Factory rules:

- `title` must be non-blank and at most 200 characters.
- `body` must be non-blank.
- `mediaSourceKey` must be non-blank and at most 200 characters.
- immediate content starts with `releasePolicy=IMMEDIATE`.
- paid content starts with `releasePolicy=PAID`.
- new content starts as `reviewStatus=PENDING` and `contentStatus=DRAFT`.

Behavior rules:

- Published content cannot be submitted for review again.
- Content cannot be published before review is approved.
- Content cannot be published before media is ready.
- Publication is idempotent.
- `ContentPublicationReady` is emitted only when content first reaches approved + media-ready + not-published.

Domain events:

- `ContentDraftCreated`
- `ContentSubmittedForReview`
- `ContentReviewApproved`
- `ContentRequiresMediaProcessing`
- `ContentMediaReady`
- `ContentPublicationReady`
- `ContentPublished`

`ContentRequiresMediaProcessing` replaces the more action-like `MediaProcessingRequested` name.

### MediaProcessingTask

`MediaProcessingTask` is the local task mirror for external media processing.

Fields:

- `contentId`
- `externalTaskId`
- `processingStatus`
- `resultSnapshot`

Behavior rules:

- A task must belong to one content id.
- `markSubmitted` requires a non-blank external task id.
- A submitted task cannot switch to a different external task id.
- A task cannot succeed before it has been submitted.
- A success snapshot must belong to the same task and external task id.
- Success is idempotent.

Domain event:

- `MediaProcessingSucceeded`

### PaidPublicationTask

`PaidPublicationTask` is the advanced paid publication process aggregate.

Fields:

- `contentId`
- `paidPublicationStatus`
- `publicationSagaId`
- `payoutHoldStatus`
- `payoutHoldId`
- `entitlementPlanStatus`
- `entitlementPlanId`
- `startedAt`
- `publishedAt`
- `completedAt`
- `failedAt`
- `failedReason`

Behavior rules:

- `recordSagaStarted` starts only from `PENDING`; same saga id retry is a no-op; different saga id is rejected.
- `recordPayoutHoldReserved` requires `RUNNING`.
- `recordPayoutHoldReleased` can release only `RESERVED`; `NONE` and `RELEASED` are no-op; `CAPTURED` is rejected.
- `recordEntitlementPlanCreated` requires `RUNNING` and payout hold `RESERVED`.
- `recordEntitlementPlanCancelled` can cancel only `CREATED`; `ACTIVATED` is rejected.
- `markPublished` requires `RUNNING`, payout hold `RESERVED`, and entitlement plan `CREATED`.
- `recordEntitlementPlanActivated` requires paid publication `PUBLISHED` and entitlement plan `CREATED`.
- `completedAt` means paid publication closed successfully after entitlement activation.
- `markFailed` is for safe failure before content publication.
- `markRequiresOperatorRepair` is for content already published, unsafe compensation, post-publish activation failure, or compensation failure.

Domain events:

- `PaidPublicationStarted`
- `CreatorPayoutHoldReserved`
- `AccessEntitlementPlanCreated`
- `PaidPublicationContentPublished`
- `AccessEntitlementPlanActivated`
- `PaidPublicationFailed`
- `PaidPublicationRequiresOperatorRepair`

These events are business facts. They do not all need subscribers.

## Value Object

`MediaProcessingResultSnapshot` remains a JSON-backed value object.

Business meaning:

- immutable accepted result from the external media processing service
- evidence used to mark a media processing task as succeeded

Rules:

- task id must be present
- content id must be present
- external task id must be non-blank
- asset hash must be non-blank
- asset location must be non-blank
- completed time must be present

Persistence carrier:

- JSON stored in `media_processing_task.result_snapshot`
- converter remains infrastructure for persistence, not the reason the value object exists

## Domain Service

Add `PaidPublicationEligibilityService`.

Purpose:

- decide whether a content item can start paid publication
- isolate the cross-aggregate decision involving `Content` and existing `PaidPublicationTask`

Inputs:

- `Content`
- optional existing `PaidPublicationTask`

Result examples:

- `Eligible`
- `AlreadyStarted`
- `NotPaidContent`
- `NotPublicationReady`
- `AlreadyPublished`

Rules:

- no repository access
- no external client access
- no UoW save
- no aggregate mutation

The command remains responsible for loading aggregates and applying state changes.

## Specification Decision

Do not enable generated aggregate specifications.

Keep:

```kotlin
specification.set(false)
```

Reasoning:

- Content publication readiness is an aggregate state invariant.
- Paid publication step ordering is an aggregate process invariant.
- Media result validation belongs to value object construction.
- Paid publication eligibility crosses aggregates and belongs in a domain service.

No standalone reusable validation policy remains that would justify a specification without duplicating aggregate behavior or weakening invariants.

If a later version adds reusable policies such as creator eligibility, content compliance, or paid listing policy, specification can be reconsidered.

## Commands

Default commands:

- `CreateContentDraftCmd`
- `CreatePaidContentDraftCmd`
- `SubmitContentForReviewCmd`
- `ApproveContentReviewCmd`
- `StartMediaProcessingCmd`
- `MarkMediaProcessingSucceededCmd`
- `RecordContentMediaReadyCmd`
- `PublishContentCmd`
- `RefreshMediaProcessingTaskStatusCmd`

Paid publication commands:

- `TryStartPaidPublicationCmd`
- `ReserveCreatorPayoutHoldCmd`
- `CreateAccessEntitlementPlanCmd`
- `PublishPaidPublicationContentCmd`
- `MarkPaidPublicationContentPublishedCmd`
- `ActivateAccessEntitlementPlanCmd`
- `CancelEntitlementPlanIfCreatedCmd`
- `ReleasePayoutHoldIfReservedCmd`
- `MarkPaidPublicationFailedCmd`

Command rules:

- one command may persist only one aggregate root
- commands may read other aggregates for zero-trust validation
- commands use `Mediator.repositories` for reads
- commands use `Mediator.factories` for new aggregates
- commands use `Mediator.uow.save()` for persistence
- commands call external capability clients only when the side effect is part of that write use case

## Queries

Queries observe only.

Required queries:

- `GetContentDetailQry`
- `GetMediaProcessingStatusQry`
- `ListSubmittedMediaProcessingTasksForPollingQry`
- `GetPaidPublicationStatusQry`

`GetContentDetailQry` should include:

- `releasePolicy`
- `mediaReadyAt`

`GetPaidPublicationStatusQry` should include:

- content id
- paid publication status
- payout hold status
- entitlement plan status
- started time
- published time
- completed time
- failure reason

## Event-Driven Continuation

Domain listeners route facts back into commands:

- `ContentRequiresMediaProcessing` -> `StartMediaProcessingCmd`
- `MediaProcessingSucceeded` -> `RecordContentMediaReadyCmd`
- `ContentPublicationReady` -> `PublishContentCmd` for immediate content
- `ContentPublicationReady` -> `TryStartPaidPublicationCmd` for paid content

Listeners must not write repositories or aggregates directly.

Commands re-load their write targets and re-check preconditions.

## Service Integration

External capability clients:

- `TriggerMediaProcessingCli`
- `GetMediaProcessingStatusCli`
- `ReserveCreatorPayoutHoldCli`
- `ReleaseCreatorPayoutHoldCli`
- `CreateAccessEntitlementPlanCli`
- `CancelAccessEntitlementPlanCli`
- `ActivateAccessEntitlementPlanCli`

External fact entry:

- inbound media processing completion event

Internal fact publication:

- outbound content published integration event

Open host service:

- HTTP controllers expose published language to users and local tools

## Inbound Integration Event

Rename the event contract from a succeeded-only name to a completed callback name:

```text
cap4k.reference.contentstudio.media-processing.completed
```

The payload may include `status`.

The subscriber maps only `status=SUCCEEDED` into:

```text
MarkMediaProcessingSucceededCmd
```

This keeps external callback semantics separate from internal domain events.

## Outbound Integration Event

Add:

```text
ContentPublishedIntegrationEvent
eventName = cap4k.reference.contentstudio.content.published
role = outbound
```

Published payload:

- `contentId`
- `releasePolicy`
- `publishedAt`

Do not expose:

- `mediaSourceKey`
- `externalTaskId`
- `payoutHoldId`
- `entitlementPlanId`

If the current runtime support is contract-only or limited, document it honestly as an outbound contract example and do not imply production-grade message delivery.

## Job

`MediaProcessingPollingFallbackJob` remains a fallback external fact observation path.

Flow:

1. Query submitted media processing tasks.
2. Send `RefreshMediaProcessingTaskStatusCmd`.
3. Command calls `GetMediaProcessingStatusCli`.
4. If the external fact is succeeded, command sends `MarkMediaProcessingSucceededCmd`.

Callback and polling must converge on the same internal success command.

## Saga

`PaidPublicationSaga` remains application orchestration.

Forward steps:

1. `ReserveCreatorPayoutHoldCmd`
2. `CreateAccessEntitlementPlanCmd`
3. `PublishPaidPublicationContentCmd`
4. `MarkPaidPublicationContentPublishedCmd`
5. `ActivateAccessEntitlementPlanCmd`

Compensation steps:

1. `CancelEntitlementPlanIfCreatedCmd`
2. `ReleasePayoutHoldIfReservedCmd`
3. `MarkPaidPublicationFailedCmd`

Saga does not write repositories or aggregates directly.

## Adapter Layering

HTTP controllers:

- translate HTTP payloads
- send command or query
- never write repository
- never invoke aggregate behavior
- never call external clients directly

Integration event subscribers:

- translate external fact payloads
- send commands
- never write state directly

Domain event subscribers:

- react to domain facts
- send commands or publish outbound integration events
- never write state directly

Client handlers:

- simulate external providers
- keep provider/protocol details out of domain and application contracts

Query handlers:

- read models and aggregate state
- never repair or mutate write state

## Generation Ownership

Source inputs:

- `design/design.json`
- `design/types.json`
- `cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql`

Evidence:

- `build/cap4k/plan.json`
- generated command/query/client/event/payload/subscriber surfaces

Rules:

- generated skeletons remain the generator-owned surface
- handwritten business logic remains in handwritten files
- generated files should not be manually patched
- ownership drift must be fixed in generation inputs or templates

## Analysis And Causal Flow

`cap4kAnalysisGenerate` produces causal flow evidence.

It is expected to show:

- controller entry flows
- inbound integration event flow
- job-related flow if supported by IR entry detection
- default publication continuation
- advanced paid publication chain where visible from code analysis

Analysis output does not define the model. It verifies that the implemented code paths reflect the model.

If a real chain is missing from flow output, treat that as analysis coverage or IR configuration evidence, not a reason to distort the business model.

## Testing Strategy

Use TDD during implementation:

- write a failing behavior test first
- verify it fails for the expected reason
- implement minimal production code
- run the focused test green
- refactor
- keep the broader suite green

Tests are public reading material in this reference project.

Required test groups:

- `ContentFactoryTest` for creation validation
- `ContentBehaviorTest` for state transitions and domain facts
- `MediaProcessingResultSnapshotTest` for value object rules
- `MediaProcessingTaskBehaviorTest` for task state rules
- `PaidPublicationTaskBehaviorTest` for process invariants
- `PaidPublicationEligibilityServiceTest` for cross-aggregate decision
- smoke tests for immediate happy path, callback path, polling fallback path, paid success, and paid compensation/repair
- outbound event contract or subscriber-level tests

After implementation, run a dedicated test cleanup pass:

- remove redundant tests
- rename unclear test cases
- keep helpers thin
- avoid opaque DSLs
- make tests read like business examples
- keep generated-code details out of behavior tests unless the test is explicitly about generation or contract

## Verification

Final verification commands:

```powershell
.\gradlew.bat cap4kPlan
.\gradlew.bat cap4kGenerate
.\gradlew.bat test
.\gradlew.bat cap4kAnalysisPlan
.\gradlew.bat cap4kAnalysisGenerate
git diff --check
git status --short
```

Report exact commands, scope, and exit status.

## Final Cleanup Gate

Before considering the branch master-ready:

- delete this file
- delete any implementation plan or temporary process file
- ensure no `docs/superpowers/specs` or `docs/superpowers/plans` files remain from this work
- ensure tests are readable as reference examples
- ensure README points to the formal `docs/modeling.md`
- ensure formal docs do not mention temporary process files
- ensure final `git status --short` contains only intended project artifacts

Final repository content should show the reference project, not the construction process.
