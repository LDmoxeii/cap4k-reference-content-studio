# Paid Publication Saga Design

Date: 2026-05-17

## Purpose

Replace the current `PublicationReleaseReadiness` Saga demonstration with a compensation-oriented paid publication Saga.

The current gated release path was added mainly to demonstrate Saga, but it demonstrates a waiting-style workflow: copyright review, manual confirmation, and release window readiness. That is not a strong Saga use case in cap4k because integration events, domain events, commands, and jobs already model waiting for facts more clearly.

The new example should show Saga where cap4k has a stronger reason to exist: a multi-step paid publication process with side effects, retry, executed-step skipping, and explicit compensation or manual repair.

## Decision

Use `PaidPublicationSaga` as the only Saga example in this project.

After the paid publication Saga is implemented and verified:

- remove `PublicationReleaseSaga`;
- remove the current `PublicationReleaseReadiness` feature;
- remove the README claim that gated release is the Saga example;
- update HTTP examples and analysis output so the advanced path points to paid publication.

Do not keep two advanced Saga-like paths. The reference project should teach one clear Saga use case, not multiple overlapping orchestration examples.

## Why Not Waiting-Style Saga

Waiting for external or future facts is not where cap4k Saga is strongest.

Better alternatives already exist:

- external system supports events: inbound integration event -> command -> domain event -> command;
- external system does not support events: job -> query pending tasks -> command refresh -> domain event -> command;
- release time window: job scans now-ready records and sends command;
- human/copyright confirmation: command records fact, domain event fan-out tries continuation.

Using Saga for these cases adds retry interval latency, not-ready exceptions, and duplicated business state without giving enough value.

Therefore, the new Saga example should not focus on waiting for a callback, waiting for a release window, or routing a simple event chain.

## Why Compensation-Style Saga

Paid publication can require multiple local transactions and external side effects:

- reserve a creator payout hold;
- create a paid access entitlement plan;
- publish content;
- activate the entitlement plan.

If a later step fails, the earlier completed steps cannot be blindly re-run. The process needs a persisted record of completed steps and a controlled recovery path.

cap4k Saga provides useful low-level runtime behavior here:

- child process records in `__saga_process`;
- `execProcess(processCode, request)` records a step;
- completed process codes are skipped during retry;
- failed Saga records can be retried by scheduled compensation or operator action.

The current runtime is still not a full compensation Saga engine. The example must be honest: compensation is hand-written through idempotent commands, and the handler must not automatically retry the forward path after compensation. Future cap4k Saga runtime improvements are tracked separately in cap4k issue #58.

## Current Runtime Constraints

The example must respect current cap4k Saga behavior:

- `SagaHandler.execProcess` is the only child-step primitive.
- There is no first-class compensation DSL.
- There is no separate compensation process state.
- There is no workflow `wait` or external signal API.
- There is no per-instance retry policy.
- `execProcess` returns cached results for `EXECUTED` process records, so automatic forward retry after compensation can skip forward steps and reuse stale compensation records.
- If a compensation command fails, the handler must preserve the primary failure and record enough diagnostic context through business state.

## Final Domain Shape

### Content

Add a paid publication path to `Content`.

Expected model changes:

- add `PAID` to `ReleasePolicy`;
- add `CreatePaidContentDraftCmd` as the explicit paid publication creation surface;
- keep default immediate/free content path as the shortest happy path;
- route paid content after media processing into paid publication rather than direct publish.

### PaidPublicationTask

Introduce `PaidPublicationTask` as a new aggregate root and the business process state carrier.

It records business facts and external resource identities. It must not duplicate every Saga runtime field.

Suggested fields:

- `id`
- `contentId`
- `paidPublicationStatus`: `PENDING`, `RUNNING`, `PUBLISHED`, `FAILED`, `REQUIRES_OPERATOR_REPAIR`
- `publicationSagaId`
- `payoutHoldStatus`: `NONE`, `RESERVED`, `RELEASED`, `CAPTURED`
- `payoutHoldId`
- `entitlementPlanStatus`: `NONE`, `CREATED`, `ACTIVATED`, `CANCELLED`
- `entitlementPlanId`
- `startedAt`
- `publishedAt`
- `completedAt`
- `failedAt`
- `failedReason`
- `dbCreatedAt`
- `dbUpdatedAt`

Expected behavior:

- create pending task idempotently for a paid content item;
- record Saga start idempotently;
- record payout hold reservation;
- record payout hold release;
- record entitlement plan creation;
- record entitlement plan cancellation;
- record entitlement plan activation;
- mark published;
- mark failed;
- mark requires operator repair when automatic compensation is unsafe.

## Command Boundaries

### Start Boundary

`TryStartPaidPublicationCmd` is the write boundary that starts the Saga.

Responsibilities:

- load content and media-processing task;
- validate content is paid, reviewed, media-ready, and not already published;
- idempotently create or find `PaidPublicationTask`;
- start `PaidPublicationSaga(taskId)` asynchronously;
- record `publicationSagaId`;
- save through UoW.

Subscriber, job, controller, and integration-event entry code must not start the Saga directly. They route to this command.

### Forward Commands

`PaidPublicationSaga` should call these forward commands through `execProcess`:

- `ReserveCreatorPayoutHoldCmd`
- `CreateAccessEntitlementPlanCmd`
- `PublishPaidPublicationContentCmd`
- `ActivateAccessEntitlementPlanCmd`

Each command must be zero-trust and idempotent:

- reload its own write target;
- validate preconditions;
- return no-op only when the desired state is already reached;
- save only aggregate roots through UoW;
- call external capability clients only when that side effect is part of the same write use case.

### Compensation Commands

Compensation commands must also be idempotent:

- `CancelEntitlementPlanIfCreatedCmd`
- `ReleasePayoutHoldIfReservedCmd`
- `MarkPaidPublicationFailedCmd`

They should inspect `PaidPublicationTask` instead of inspecting Saga process tables. Business facts decide whether compensation is meaningful; Saga process records decide whether a command has already executed.

## Saga Handler Shape

The current-runtime implementation will be explicit and lower-level:

```kotlin
object PaidPublicationSaga {

    const val PROCESS_RESERVE_PAYOUT_HOLD = "reserve-payout-hold"
    const val PROCESS_CREATE_ENTITLEMENT_PLAN = "create-entitlement-plan"
    const val PROCESS_PUBLISH_CONTENT = "publish-content"
    const val PROCESS_ACTIVATE_ENTITLEMENT_PLAN = "activate-entitlement-plan"

    const val PROCESS_CANCEL_ENTITLEMENT_PLAN = "cancel-entitlement-plan-if-created"
    const val PROCESS_RELEASE_PAYOUT_HOLD = "release-payout-hold-if-reserved"
    const val PROCESS_MARK_PUBLICATION_FAILED = "mark-paid-publication-failed"

    // Current cap4k Saga process caching makes automatic forward retry unsafe after compensation.
    // cap4k issue #58 tracks first-class compensation runtime support.
    @Retry(
        retryTimes = 1,
        retryIntervals = [1],
        expireAfter = 1440,
    )
    data class Request(
        val paidPublicationTaskId: UUID,
    ) : SagaParam<Response>

    data class Response(
        val published: Boolean,
    )

    class Handler : SagaHandler<Request, Response> {

        override fun exec(request: Request): Response {
            return try {
                runForward(request)
                Response(published = true)
            } catch (primary: Throwable) {
                val compensationFailures = compensateBestEffort(request, primary)
                if (compensationFailures.isNotEmpty()) {
                    compensationFailures.forEach(primary::addSuppressed)
                    throw primary
                }
                Response(published = false)
            }
        }

        private fun runForward(request: Request) {
            execProcess(
                PROCESS_RESERVE_PAYOUT_HOLD,
                ReserveCreatorPayoutHoldCmd.Request(request.paidPublicationTaskId),
            )
            execProcess(
                PROCESS_CREATE_ENTITLEMENT_PLAN,
                CreateAccessEntitlementPlanCmd.Request(request.paidPublicationTaskId),
            )
            execProcess(
                PROCESS_PUBLISH_CONTENT,
                PublishPaidPublicationContentCmd.Request(request.paidPublicationTaskId),
            )
            execProcess(
                PROCESS_ACTIVATE_ENTITLEMENT_PLAN,
                ActivateAccessEntitlementPlanCmd.Request(request.paidPublicationTaskId),
            )
        }

        private fun compensateBestEffort(request: Request, primary: Throwable): List<Throwable> {
            val compensationFailures = mutableListOf<Throwable>()

            runCompensation(compensationFailures) {
                execProcess(
                    PROCESS_CANCEL_ENTITLEMENT_PLAN,
                    CancelEntitlementPlanIfCreatedCmd.Request(
                        paidPublicationTaskId = request.paidPublicationTaskId,
                        reason = primary.message ?: "Paid publication saga failed.",
                    ),
                )
            }

            runCompensation(compensationFailures) {
                execProcess(
                    PROCESS_RELEASE_PAYOUT_HOLD,
                    ReleasePayoutHoldIfReservedCmd.Request(
                        paidPublicationTaskId = request.paidPublicationTaskId,
                        reason = primary.message ?: "Paid publication saga failed.",
                    ),
                )
            }

            runCompensation(compensationFailures) {
                execProcess(
                    PROCESS_MARK_PUBLICATION_FAILED,
                    MarkPaidPublicationFailedCmd.Request(
                        paidPublicationTaskId = request.paidPublicationTaskId,
                        failedReason = buildFailureReason(primary, compensationFailures),
                    ),
                )
            }

            return compensationFailures
        }
    }
}
```

This is intentionally not presented as the ideal final cap4k Saga API. It is the correct current-runtime shape until cap4k issue #58 introduces better compensation support. A forward failure with successful compensation completes with `published = false`; only compensation failure rethrows the primary failure with suppressed compensation failures.

## Manual Repair Boundary

Some failures are not safely compensatable.

The key example is:

```text
publish-content succeeds
activate-entitlement-plan fails
```

The example should not pretend content publication can always be rolled back. If automatic compensation would violate business semantics, the task should become `REQUIRES_OPERATOR_REPAIR` with a clear reason.

This teaches an important Saga rule: compensation is business behavior, not automatic technical undo.

## PublicationReleaseReadiness Simplification

`PublicationReleaseReadiness` should not remain a Saga example.

After `PaidPublicationSaga` is in place:

- remove `PublicationReleaseSaga`;
- remove `PublicationReleaseReadiness`, its commands, queries, factory, behavior, events, subscribers, job, HTTP endpoints, tests, generated snapshots, and schema;
- remove README text that calls gated release a real Saga example;
- remove `advanced-release-readiness.http`;
- regenerate analysis output after code changes.

Final state: delete the gated release readiness feature from the reference project.

Reason:

- it was introduced to demonstrate Saga;
- it now teaches the wrong Saga lesson;
- the project already has job and callback examples elsewhere;
- keeping it creates overlapping advanced paths and increases reader cognitive load.

## Documentation Impact

README and public-facing explanations should say:

- default path: content review -> media processing -> media callback -> publish;
- advanced Saga path: paid publication with compensatable side effects;
- Saga is not the default tool for waiting on callbacks, time windows, or simple event fan-out;
- current cap4k Saga supports process record/retry/skip, while first-class compensation runtime is future work tracked in cap4k issue #58.

## Verification Expectations

Implementation should prove:

- paid content creates or finds one `PaidPublicationTask`;
- duplicate start attempts converge to the same task/Saga-start state;
- forward Saga steps record payout hold, entitlement plan, content publish, and entitlement activation;
- retry skips already executed forward steps;
- forward failure triggers idempotent compensation commands;
- compensation commands no-op when their target side effect was never created;
- content already published plus entitlement activation failure marks `REQUIRES_OPERATOR_REPAIR`;
- default immediate content path still works;
- old gated release Saga documentation is gone or explicitly marked non-Saga.

## Related Upstream Work

- cap4k issue #58: `saga: enhance compensation-oriented runtime support`
- reference framework to study: `Ahoo-Wang/Wow`, especially event-driven stateless Saga and handler-level retry
