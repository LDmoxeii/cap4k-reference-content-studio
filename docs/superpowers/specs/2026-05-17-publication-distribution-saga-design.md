# Publication Distribution Saga Design

Date: 2026-05-17

## Purpose

Replace the current publication-release Saga demonstration with a stricter Saga example that matches cap4k's actual Saga runtime.

The example should demonstrate a long-running external distribution process:

1. publication release readiness becomes eligible;
2. the system creates a business task for publication distribution;
3. a Saga submits the distribution request to an external distribution capability;
4. an external success fact eventually marks the distribution task as succeeded;
5. Saga retry later observes the succeeded business fact and publishes the content.

The example intentionally does not implement immediate callback-driven Saga resume. The trade-off must be explicit: publication after the callback is delayed until the next Saga compensation/retry.

## Non-Goals

- Do not model Saga as a workflow engine with `wait`, `signal`, or explicit step resume.
- Do not call `SagaManager.retry` from callback, subscriber, controller, or job in this version.
- Do not introduce `TryResumePublicationDistributionSagaCmd` in this version.
- Do not store distribution business state in cap4k Saga runtime tables.
- Do not make `PublicationReleaseReadiness` own CDN/media distribution state.
- Do not use provider-specific names such as CDN or OSS in domain-facing names unless they are only explanatory documentation text.

## Runtime Constraint

cap4k Saga is a persisted orchestration/retry mechanism:

- `SagaHandler.execProcess(processCode, request)` records a child process.
- An executed process is skipped on later retries and returns its cached result.
- A failed process marks the Saga as failed/exception and leaves it eligible for scheduled compensation.
- There is no first-class external callback signal API.
- There is no per-instance dynamic retry interval; retry intervals are configured by Saga request type through `@Retry`, otherwise defaults apply.

Therefore, waiting for external distribution success must be modeled as a retryable check against business state, not as a blocking wait.

## Domain Model

Introduce `PublicationDistributionTask` as a new aggregate root.

It is the business state carrier for external publication distribution. It is not a Saga runtime record and not a child of `PublicationReleaseReadiness`.

Fields:

- `id`
- `contentId`
- `readinessId`
- `distributionStatus`: `PENDING`, `SUBMITTED`, `SUCCEEDED`, `FAILED`
- `externalDistributionTaskId`
- `distributionSagaId`
- `submittedAt`
- `completedAt`
- `failedReason`
- `dbCreatedAt`
- `dbUpdatedAt`

Expected aggregate behavior:

- create a pending distribution task for an eligible readiness record;
- record the started Saga ID idempotently;
- record external distribution submission idempotently;
- mark distribution succeeded idempotently from an external fact;
- mark distribution failed when the external fact says the distribution failed or the external capability reports a terminal failure.

## Command Boundaries

`TryContinuePublicationReleaseCmd` remains the write boundary that starts the long-running process.

Responsibilities:

- load and validate `PublicationReleaseReadiness`;
- confirm readiness can enter publication distribution;
- idempotently create or find `PublicationDistributionTask(PENDING)`;
- start `PublicationDistributionSaga(taskId)` asynchronously;
- record `distributionSagaId` on the distribution task;
- save through UoW.

Subscriber, job, controller, and external fact entry code must not directly start the Saga. They should route to command.

## Saga Design

`PublicationDistributionSaga` coordinates the external distribution reliability process. It does not decide publication eligibility.

Saga request:

- `taskId`

Saga process sequence:

1. `complete-release-readiness`
   - Command: `CompletePublicationReleaseReadinessCmd`
   - Purpose: close the readiness gate after distribution has started.

2. `submit-publication-distribution`
   - Command: `SubmitPublicationDistributionCmd`
   - Purpose: call the external distribution capability and record `SUBMITTED` plus `externalDistributionTaskId`.

3. `ensure-publication-distribution-succeeded`
   - Command: `EnsurePublicationDistributionSucceededCmd`
   - Purpose: verify `PublicationDistributionTask` is `SUCCEEDED`.
   - If not succeeded, this command must fail with a retryable/not-ready exception. It must not return no-op success, because `execProcess` would cache the process as executed.

4. `publish-content`
   - Command: `PublishContentCmd(releaseReadinessSatisfied = true)`
   - Purpose: publish only after the distribution success fact is present.

## External Interaction

Internal consumes external:

- external capability client: publication distribution / media distribution client;
- used by `SubmitPublicationDistributionCmd`;
- provider-specific protocol is translated in adapter code.

External consumes internal:

- external fact entry: publication distribution callback or inbound integration event;
- routes to `MarkPublicationDistributionSucceededCmd` or a failure command;
- updates only `PublicationDistributionTask`;
- does not publish content;
- does not directly resume the Saga.

## Retry And Latency Trade-Off

This design accepts delayed publication after the external success callback.

Flow:

```text
callback
  -> MarkPublicationDistributionSucceededCmd
  -> PublicationDistributionTask(SUCCEEDED)
  -> no immediate Saga resume

scheduled Saga compensation
  -> retry PublicationDistributionSaga
  -> skip already executed complete/submit processes
  -> ensure distribution succeeded
  -> publish content
```

This is intentional for the example because it demonstrates the actual cap4k Saga runtime without pretending it has workflow signal semantics.

If a real product requires near-real-time publication after callback, this example is not enough. That product should add an explicit, guarded resume command or use a workflow engine with durable signal support.

## Failure Semantics

- Duplicate start attempts converge through idempotent task creation and Saga ID recording.
- Duplicate callbacks converge through idempotent distribution success/failure commands.
- Missing task/readiness/content is an invariant violation and should fail.
- Distribution not yet succeeded is a retryable not-ready condition inside the Saga check step.
- Terminal distribution failure should update `PublicationDistributionTask(FAILED)` and prevent publish.

## Documentation Impact

The README and public authoring docs should describe Saga as an advanced capability:

- use Saga only for persisted long-running coordination, retry, recovery, or cross-time waiting;
- do not use Saga for ordinary event fan-out or simple callback-to-command flows;
- explain that this example accepts retry-interval latency after callback;
- state that cap4k Saga is orchestration/retry, not a full workflow engine.

## Verification Expectations

Implementation should prove:

- eligible readiness starts exactly one distribution task and one Saga ID;
- Saga submit step records external distribution submission;
- Saga retry before external success does not publish content;
- callback marks the distribution task succeeded without publishing directly;
- later Saga retry publishes content and skips already executed process steps;
- duplicate callbacks and duplicate continuation attempts are idempotent.
