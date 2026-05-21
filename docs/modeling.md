# Modeling Guide

This document is the public modeling reading surface for
`cap4k-reference-content-studio`. It explains how to read the reference project
as a cap4k business model before jumping into generated files or framework
internals.

## Purpose

The project models a small content studio where an author creates content, an
editor approves it, media processing completes, and the content becomes
published. The default path stays intentionally small and local. Advanced paid
publication is present only as an opt-in example for compensation-oriented
coordination.

Use this guide when you want to understand why the project contains commands,
queries, repositories, factories, value objects, a domain service, domain
events, integration events, external clients, a job, and a Saga.

## Business Vocabulary

- Content is the editorial item being drafted, reviewed, made media-ready, and
  published.
- Review is the editorial approval decision required before publication.
- Media processing is the external work that prepares the content asset.
- Media ready means the media-processing result has been accepted into the
  content model.
- Publication ready means review approval and media readiness are both true,
  but the content has not yet been published.
- Release policy decides whether ready content follows immediate publication or
  the paid publication path.
- Paid publication task records the paid publication state across payout hold,
  entitlement plan, publication, activation, failure, and repair.
- Payout hold and entitlement plan are external paid-publication capabilities,
  represented through clients in this local reference app.

## Bounded Context

The bounded context is a single local content studio. It owns the editorial
lifecycle, the local media-processing task record, and the local paid publication
task record.

External systems are deliberately small and fake in version one:

- Media processing is represented by a trigger/status client and an inbound
  callback integration event.
- Paid publication capabilities are represented by payout and entitlement
  clients.
- Published content is emitted as an outbound integration event for other
  contexts.

The project is a reference application, not a production platform. It keeps one
Spring Boot process, one H2 runtime, committed `.http` examples, OpenAPI
snapshots, and analysis outputs so the model can be read and run locally.

## Default Immediate Publication Flow

The default flow is immediate publication after review and media readiness.

1. `CreateContentDraftCmd` creates a `Content` aggregate through
   `ContentFactory`.
2. `SubmitContentForReviewCmd` moves the content into review.
3. `ApproveContentReviewCmd` approves review. If media is not ready, `Content`
   emits `ContentRequiresMediaProcessingDomainEvent`.
4. `ContentRequiresMediaProcessingDomainEventSubscriber` routes that fact to
   `StartMediaProcessingCmd`, which creates or starts a `MediaProcessingTask`.
5. The HTTP integration callback is consumed as
   `MediaProcessingCallbackIntegrationEvent` and routed to
   `MarkMediaProcessingSucceededCmd`.
6. `MediaProcessingTask` records the result and emits
   `MediaProcessingSucceededDomainEvent`.
7. `MediaProcessingSucceededDomainEventSubscriber` routes the fact to
   `RecordContentMediaReadyCmd`.
8. `Content` records media readiness and emits
   `ContentPublicationReadyDomainEvent` once review approval and media
   readiness are both present.
9. `ContentPublicationReadyDomainEventSubscriber` sends `PublishContentCmd` for
   immediate content.
10. `Content` publishes and emits `ContentPublishedDomainEvent`.
11. `ContentPublishedDomainEventSubscriber` attaches
    `ContentPublishedIntegrationEvent` through the runtime publication API.

The outbound integration event is actually wired through
`Mediator.events.attach(...)`, not just declared as a contract-only event.

## Advanced Paid Publication Flow

Paid publication is opt-in. It starts only for content created with the paid
release policy and only after the same publication-ready facts are present.

`ContentPublicationReadyDomainEventSubscriber` also sends
`TryStartPaidPublicationCmd`. That command loads `Content`, checks for an
existing `PaidPublicationTask`, asks `PaidPublicationEligibilityService` whether
the paid path may start, creates the task through `PaidPublicationTaskFactory`
when needed, schedules `PaidPublicationSaga`, records the Saga id, and saves the
unit of work.

`PaidPublicationSaga` then executes the forward steps:

1. reserve creator payout hold;
2. create access entitlement plan;
3. publish the content;
4. mark the paid publication task as content-published;
5. activate the entitlement plan.

If a later step fails, the Saga performs best-effort compensation by cancelling
the entitlement plan if created, releasing the payout hold if reserved, and
marking the paid publication failed. If compensation cannot safely complete, the
task records that operator repair is required.

This path demonstrates a real compensation Saga. It is not the default
publication path and should not be read as required ceremony for ordinary
content publication.

## Aggregate Boundaries

`Content` owns editorial consistency:

- draft text and media source;
- review status and reviewer facts;
- media readiness;
- release policy;
- published state;
- publication readiness and publication domain facts.

`MediaProcessingTask` owns media-processing consistency:

- local task status;
- external task identity;
- accepted processing result;
- media-processing success domain fact.

`PaidPublicationTask` owns paid-publication coordination state:

- Saga id and task lifecycle;
- payout hold state;
- entitlement plan state;
- publication, completion, failure, and repair markers.

Commands may read other aggregates for zero-trust validation, but each command
keeps write ownership narrow. The aggregate being changed remains the owner of
its own invariant transitions.

## Value Object

`MediaProcessingResultSnapshot` is modeled as a value object under
`MediaProcessingTask`. It captures the accepted media result, trims stable
string inputs, validates that the asset SHA-256 is a 64-character hexadecimal
value, normalizes the hash to lowercase, and derives a deterministic id from the
business result fields.

In this reference project it is persisted through the
`media_processing_task.result_snapshot` JSON-backed column. Treat it as a
handwritten value concept used to make the example readable. It should not be
read as complete generator support for every value object persistence style.

## Domain Service

`PaidPublicationEligibilityService` is a domain service because the decision to
start paid publication does not belong entirely to one aggregate method. It
looks at the `Content` release policy and readiness together with the existing
`PaidPublicationTask` state.

The service returns a business decision such as eligible, not paid content, not
publication ready, already started, or already published. The command uses that
decision to either schedule the Saga or converge as an idempotent no-op.

## Why Specification Is Not Used

This project intentionally keeps aggregate specification generation disabled
because no standalone reusable validation policy remains after aggregate
invariants, value object rules, and the paid publication domain service are
modeled.

The important rules already have clearer homes:

- aggregate methods protect state transitions;
- factories protect creation inputs;
- `MediaProcessingResultSnapshot` protects value semantics;
- `PaidPublicationEligibilityService` protects the cross-aggregate paid
  publication start decision.

Adding a Specification here would make the example noisier without giving
readers a reusable policy object to study.

## Command, Query, Repository, Factory, And UoW Boundaries

Commands express user or process intentions. They load the required aggregate
through `Mediator.repositories`, call aggregate behavior, create new aggregates
through `Mediator.factories` when needed, send downstream requests through
`Mediator.cmd`, `Mediator.requests`, or external clients, and commit through
`Mediator.uow.save()`.

Queries observe state for the HTTP read surface. They do not repair or mutate
the write model.

Repositories are used as aggregate access boundaries. They locate the aggregate
or local task record needed by a command or query, but they do not own business
decisions.

Factories own creation-time validation and default state. `ContentFactory`
normalizes draft input and chooses the initial review/content state supplied by
the command. `PaidPublicationTaskFactory` creates a pending task with empty
payout and entitlement state.

The unit of work boundary is explicit: command handlers call `Mediator.uow.save()`
after aggregate state changes and generated events have been attached.

## Service Integration

The project shows three service-integration shapes.

Inbound integration event:
`MediaProcessingCallbackIntegrationEvent` represents the external
media-processing completion callback. Its subscriber routes the external fact to
`MarkMediaProcessingSucceededCmd`, so the aggregate remains the owner of local
state changes.

External capability clients:
media-processing and paid-publication `Cli` request types represent
capabilities outside the content studio. The local handlers are fake adapters
so the reference app is runnable without real external systems.

Outbound integration event:
`ContentPublishedIntegrationEvent` is emitted when the content-published domain
fact occurs. It is not only a contract type; it is attached through the runtime
publication API by `ContentPublishedDomainEventSubscriber`.

## Saga

`PaidPublicationSaga` is used only where the model needs persisted,
cross-step, compensation-oriented coordination. It coordinates external payout
and entitlement capabilities with the local content publication command and the
paid-publication task record.

The Saga is not used for the default immediate publication path because that
path does not need long-running compensation. The immediate path can be
expressed as domain facts and commands.

## Job

`MediaProcessingPollingFallbackJob` is a fallback entry point. Callback remains
the main path. The job reads submitted media-processing tasks through
`ListSubmittedMediaProcessingTasksForPollingQry` and routes each external
observation back into `RefreshMediaProcessingTaskStatusCmd`.

That keeps polling and callback behavior converged on the same internal command
surface instead of creating a second write model.

## Generation Ownership

The public reading order is:

1. `design/design.json`;
2. the database schema;
3. `build/cap4k/plan.json` from a fresh plan run;
4. the checked-in source under each module's `src/main/kotlin`;
5. the tests and `.http` files that exercise the model.

Generator-owned surfaces include the declared command, query, client, payload,
subscriber, event, repository, and factory scaffolding. Handwritten code fills
the business behavior inside the generated structure where the example needs
real domain decisions.

Do not read historical generated snapshots as the truth source for this
repository. The current model is the combination of declared inputs, current
generation plan, checked-in source, and executable examples.

## Analysis And Causal Flow

The analysis flow files are generated evidence from code. They are not business
modeling inputs.

Use `analysis/flows/*.json` and `analysis/flows/*.mmd` after reading the model
and code. They help inspect how controllers, subscribers, jobs, commands, and
queries connect in the current implementation. They should not be used to
invent domain boundaries or override the business vocabulary.

## Testing As Example Documentation

Tests are part of the reading surface:

- domain behavior tests show aggregate state transitions and emitted facts;
- factory tests show creation-time validation and defaults;
- value object tests show normalization and validation;
- domain service tests show paid-publication eligibility decisions;
- command and Saga tests show application-level orchestration and idempotence;
- smoke tests show the runnable HTTP happy path and the opt-in paid publication
  path.

Read tests as executable examples. They are intentionally more useful to a
reader than a static diagram because they prove the model still runs.
