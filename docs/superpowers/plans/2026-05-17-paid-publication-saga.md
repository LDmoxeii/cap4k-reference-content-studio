# Paid Publication Saga Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the old waiting-style release-readiness Saga example with a paid-publication compensation Saga and remove the old gated release feature.

**Architecture:** `design/design.json` and `content-studio-schema.sql` stay the generated-surface source of truth. A new `PaidPublicationTask` aggregate carries business process state; `TryStartPaidPublicationCmd` starts `PaidPublicationSaga`; Saga forward and compensation steps call idempotent commands. The old `PublicationReleaseReadiness` path is deleted after the paid Saga path is working.

**Tech Stack:** Kotlin, Spring Boot, cap4k pipeline, cap4k DDD runtime, H2, JUnit 5, AssertJ, Gradle wrapper.

---

## File Structure

Generated input files:

- Modify: `design/design.json`
- Modify: `cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql`
- Modify: `build.gradle.kts`

Generation ownership evidence after `cap4kPlan` and `cap4kGenerate`:

- Inspect: `build/cap4k/plan.json`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/**` for generator-owned design surfaces when generation updates checked-in files
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/**` for both checked-in generator-owned and handwritten code, with ownership decided by fresh `build/cap4k/plan.json`
- Modify: `cap4k-reference-content-studio-adapter/src/main/kotlin/**` for both checked-in generator-owned and handwritten code, with ownership decided by fresh `build/cap4k/plan.json`

Handwritten domain files:

- Create: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/paid_publication_task/PaidPublicationTaskBehavior.kt`
- Create: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/paid_publication_task/factory/PaidPublicationTaskFactory.kt`
- Create: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/paid_publication_task/PaidPublicationTaskBehaviorTest.kt`
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/factory/ContentFactory.kt`
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/services/PublicationEligibilityDomainService.kt`
- Modify: existing content/domain tests that mention `ReleasePolicy.GATED` or release windows

Handwritten application files:

- Create: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/paid/publication/TryStartPaidPublicationCmd.kt`
- Create: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/paid/publication/ReserveCreatorPayoutHoldCmd.kt`
- Create: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/paid/publication/CreateAccessEntitlementPlanCmd.kt`
- Create: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/paid/publication/PublishPaidPublicationContentCmd.kt`
- Create: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/paid/publication/ActivateAccessEntitlementPlanCmd.kt`
- Create: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/paid/publication/CancelEntitlementPlanIfCreatedCmd.kt`
- Create: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/paid/publication/ReleasePayoutHoldIfReservedCmd.kt`
- Create: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/paid/publication/MarkPaidPublicationFailedCmd.kt`
- Create: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/sagas/paid/publication/PaidPublicationSaga.kt`
- Create: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/distributed/clients/paid/publication/ReserveCreatorPayoutHoldCli.kt`
- Create: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/distributed/clients/paid/publication/ReleaseCreatorPayoutHoldCli.kt`
- Create: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/distributed/clients/paid/publication/CreateAccessEntitlementPlanCli.kt`
- Create: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/distributed/clients/paid/publication/CancelAccessEntitlementPlanCli.kt`
- Create: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/distributed/clients/paid/publication/ActivateAccessEntitlementPlanCli.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/domain/media_processing_task/MediaProcessingSucceededDomainEventSubscriber.kt`

Handwritten adapter/start/docs files:

- Create: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/http/AdvancedPaidPublicationController.kt`
- Create: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/application/distributed/clients/paid/publication/FakePaidPublicationCliHandlers.kt`
- Create: `cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/ContentStudioPaidPublicationSagaHttpSmokeTest.kt`
- Create: `http/paid-publication.http`
- Modify: `README.md`
- Modify: `README.zh-CN.md`
- Regenerate: `openapi/content-studio-openapi.json`
- Regenerate: `analysis/flows/**`

Delete old release-readiness feature files:

- Delete: `http/advanced-release-readiness.http`
- Delete: `cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/ContentStudioAdvancedReleaseReadinessHttpSmokeTest.kt`
- Delete: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/http/AdvancedReleaseReadinessController.kt`
- Delete: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/application/queries/release/readiness/ListPublicationReleaseReadinessReadyToContinueQryHandler.kt`
- Delete: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/domain/repositories/PublicationReleaseReadinessRepository.kt` if it still exists as a checked-in handwritten file
- Delete: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/release/readiness/*.kt`
- Delete: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/queries/release/readiness/ListPublicationReleaseReadinessReadyToContinueQry.kt`
- Delete: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/domain/publication_release_readiness/*.kt`
- Delete: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/jobs/PublicationReleaseContinuationJob.kt`
- Delete: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/sagas/publication/PublicationReleaseSaga.kt`
- Delete: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/publication_release_readiness/**`
- Delete: generator-owned `publication_release_readiness` domain surfaces only if they remain checked in under `src/main/kotlin/**`
- Delete: handwritten compatibility helpers for `publication_release_readiness` only if they remain checked in under `src/main/kotlin/**`
- Delete: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/publication_release_readiness/PublicationReleaseReadinessBehaviorTest.kt`
- Delete old `analysis/flows/*AdvancedReleaseReadinessCon*`

## Task 1: Update Design And Schema Inputs

**Files:**

- Modify: `design/design.json`
- Modify: `cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql`
- Modify: `build.gradle.kts`

- [ ] **Step 1: Add paid schema source while keeping release readiness**

Replace the `content.release_policy` enum comment with the transition enum that keeps existing gated content compatible and adds paid publication:

```sql
release_policy int not null comment '@T=ReleasePolicy;@E=0:IMMEDIATE:Immediate|1:GATED:Gated|2:PAID:Paid;',
```

Keep these columns in `content` until Task 7 deletes the old release-readiness path:

```sql
release_window_opens_at timestamp,
release_window_closes_at timestamp,
```

Keep the full `publication_release_readiness` table until Task 7.

Add the new table:

```sql
create table if not exists paid_publication_task (
    id uuid primary key,
    content_id uuid not null,
    paid_publication_status int not null comment '@T=PaidPublicationStatus;@E=0:PENDING:Pending|1:RUNNING:Running|2:PUBLISHED:Published|3:FAILED:Failed|4:REQUIRES_OPERATOR_REPAIR:Requires operator repair;',
    publication_saga_id varchar(64),
    payout_hold_status int not null comment '@T=PayoutHoldStatus;@E=0:NONE:None|1:RESERVED:Reserved|2:RELEASED:Released|3:CAPTURED:Captured;',
    payout_hold_id varchar(120),
    entitlement_plan_status int not null comment '@T=EntitlementPlanStatus;@E=0:NONE:None|1:CREATED:Created|2:ACTIVATED:Activated|3:CANCELLED:Cancelled;',
    entitlement_plan_id varchar(120),
    started_at timestamp,
    published_at timestamp,
    completed_at timestamp,
    failed_at timestamp,
    failed_reason varchar(1000),
    db_created_at timestamp not null,
    db_updated_at timestamp not null,
    constraint uq_paid_publication_task_content_id unique (content_id),
    constraint fk_paid_publication_task_content foreign key (content_id) references content(id)
);
```

- [ ] **Step 2: Update cap4k DB include tables**

In `build.gradle.kts`, replace:

```kotlin
includeTables.set(
    listOf(
        "content",
        "media_processing_task",
        "publication_release_readiness"
    )
)
```

with:

```kotlin
includeTables.set(
    listOf(
        "content",
        "media_processing_task",
        "publication_release_readiness",
        "paid_publication_task"
    )
)
```

- [ ] **Step 3: Update design JSON**

Keep every design object for the old gated release path until Task 7:

```text
CopyrightReviewPassed
ManualReleaseConfirmed
CreateGatedContentDraft
OpenPublicationReleaseReadiness
PassCopyrightReview
ConfirmManualRelease
CompletePublicationReleaseReadiness
TryContinuePublicationRelease
ListPublicationReleaseReadinessReadyToContinue
CreateGatedContentDraftPayload
```

Also add these paid publication design objects. Keep package names exactly as shown so generated files land in predictable folders:

```json
{
  "tag": "command",
  "package": "content.workflow",
  "name": "CreatePaidContentDraft",
  "desc": "create paid content draft",
  "aggregates": ["Content"],
  "requestFields": [
    { "name": "title", "type": "String" },
    { "name": "body", "type": "String" },
    { "name": "mediaSourceKey", "type": "String" }
  ],
  "responseFields": [
    { "name": "contentId", "type": "java.util.UUID" }
  ]
}
```

```json
{
  "tag": "api_payload",
  "package": "content.workflow",
  "name": "CreatePaidContentDraftPayload",
  "desc": "create paid content draft http payload",
  "aggregates": [],
  "requestFields": [
    { "name": "title", "type": "String" },
    { "name": "body", "type": "String" },
    { "name": "mediaSourceKey", "type": "String" }
  ],
  "responseFields": [
    { "name": "contentId", "type": "java.util.UUID" }
  ]
}
```

```json
{
  "tag": "command",
  "package": "paid.publication",
  "name": "TryStartPaidPublication",
  "desc": "try starting paid publication saga",
  "aggregates": ["PaidPublicationTask"],
  "requestFields": [
    { "name": "contentId", "type": "java.util.UUID" }
  ],
  "responseFields": [
    { "name": "taskId", "type": "java.util.UUID", "nullable": true },
    { "name": "started", "type": "Boolean" }
  ]
}
```

```json
{
  "tag": "command",
  "package": "paid.publication",
  "name": "ReserveCreatorPayoutHold",
  "desc": "reserve creator payout hold for paid publication",
  "aggregates": ["PaidPublicationTask"],
  "requestFields": [
    { "name": "paidPublicationTaskId", "type": "java.util.UUID" }
  ],
  "responseFields": [
    { "name": "reserved", "type": "Boolean" }
  ]
}
```

```json
{
  "tag": "command",
  "package": "paid.publication",
  "name": "CreateAccessEntitlementPlan",
  "desc": "create access entitlement plan for paid publication",
  "aggregates": ["PaidPublicationTask"],
  "requestFields": [
    { "name": "paidPublicationTaskId", "type": "java.util.UUID" }
  ],
  "responseFields": [
    { "name": "created", "type": "Boolean" }
  ]
}
```

```json
{
  "tag": "command",
  "package": "paid.publication",
  "name": "PublishPaidPublicationContent",
  "desc": "publish content for paid publication saga",
  "aggregates": ["PaidPublicationTask"],
  "requestFields": [
    { "name": "paidPublicationTaskId", "type": "java.util.UUID" }
  ],
  "responseFields": [
    { "name": "published", "type": "Boolean" }
  ]
}
```

```json
{
  "tag": "command",
  "package": "paid.publication",
  "name": "ActivateAccessEntitlementPlan",
  "desc": "activate access entitlement plan for paid publication",
  "aggregates": ["PaidPublicationTask"],
  "requestFields": [
    { "name": "paidPublicationTaskId", "type": "java.util.UUID" }
  ],
  "responseFields": [
    { "name": "activated", "type": "Boolean" }
  ]
}
```

```json
{
  "tag": "command",
  "package": "paid.publication",
  "name": "CancelEntitlementPlanIfCreated",
  "desc": "cancel entitlement plan if it was created for failed paid publication",
  "aggregates": ["PaidPublicationTask"],
  "requestFields": [
    { "name": "paidPublicationTaskId", "type": "java.util.UUID" },
    { "name": "reason", "type": "String" }
  ],
  "responseFields": [
    { "name": "cancelled", "type": "Boolean" }
  ]
}
```

```json
{
  "tag": "command",
  "package": "paid.publication",
  "name": "ReleasePayoutHoldIfReserved",
  "desc": "release payout hold if it was reserved for failed paid publication",
  "aggregates": ["PaidPublicationTask"],
  "requestFields": [
    { "name": "paidPublicationTaskId", "type": "java.util.UUID" },
    { "name": "reason", "type": "String" }
  ],
  "responseFields": [
    { "name": "released", "type": "Boolean" }
  ]
}
```

```json
{
  "tag": "command",
  "package": "paid.publication",
  "name": "MarkPaidPublicationFailed",
  "desc": "mark paid publication failed after saga failure or compensation failure",
  "aggregates": ["PaidPublicationTask"],
  "requestFields": [
    { "name": "paidPublicationTaskId", "type": "java.util.UUID" },
    { "name": "failedReason", "type": "String" }
  ],
  "responseFields": []
}
```

Add client design objects for fake external capabilities:

```json
{
  "tag": "client",
  "package": "paid.publication",
  "name": "ReserveCreatorPayoutHold",
  "desc": "reserve payout hold in fake creator settlement service",
  "aggregates": ["PaidPublicationTask"],
  "requestFields": [
    { "name": "paidPublicationTaskId", "type": "java.util.UUID" }
  ],
  "responseFields": [
    { "name": "payoutHoldId", "type": "String" }
  ]
}
```

```json
{
  "tag": "client",
  "package": "paid.publication",
  "name": "ReleaseCreatorPayoutHold",
  "desc": "release payout hold in fake creator settlement service",
  "aggregates": ["PaidPublicationTask"],
  "requestFields": [
    { "name": "paidPublicationTaskId", "type": "java.util.UUID" }
  ],
  "responseFields": [
    { "name": "released", "type": "Boolean" }
  ]
}
```

```json
{
  "tag": "client",
  "package": "paid.publication",
  "name": "CreateAccessEntitlementPlan",
  "desc": "create access entitlement plan in fake entitlement service",
  "aggregates": ["PaidPublicationTask"],
  "requestFields": [
    { "name": "paidPublicationTaskId", "type": "java.util.UUID" }
  ],
  "responseFields": [
    { "name": "entitlementPlanId", "type": "String" }
  ]
}
```

```json
{
  "tag": "client",
  "package": "paid.publication",
  "name": "CancelAccessEntitlementPlan",
  "desc": "cancel access entitlement plan in fake entitlement service",
  "aggregates": ["PaidPublicationTask"],
  "requestFields": [
    { "name": "paidPublicationTaskId", "type": "java.util.UUID" }
  ],
  "responseFields": [
    { "name": "cancelled", "type": "Boolean" }
  ]
}
```

```json
{
  "tag": "client",
  "package": "paid.publication",
  "name": "ActivateAccessEntitlementPlan",
  "desc": "activate access entitlement plan in fake entitlement service",
  "aggregates": ["PaidPublicationTask"],
  "requestFields": [
    { "name": "paidPublicationTaskId", "type": "java.util.UUID" }
  ],
  "responseFields": [
    { "name": "activated", "type": "Boolean" }
  ]
}
```

- [ ] **Step 4: Run generation**

Run:

```powershell
.\gradlew.bat cap4kPlan cap4kGenerate
```

Expected: both tasks complete successfully.

- [ ] **Step 5: Inspect generated ownership**

Run:

```powershell
rg -n "PaidPublicationTask|paid_publication_task|CreatePaidContentDraft|TryStartPaidPublication|ReserveCreatorPayoutHold|CreateAccessEntitlementPlan" design cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql build/cap4k/plan.json cap4k-reference-content-studio-application/src/main/kotlin
```

Expected: paid publication ownership evidence exists in design, schema, `build/cap4k/plan.json`, and application surfaces. Old `PublicationReleaseReadiness` references are expected to remain until Task 7.

- [ ] **Step 6: Commit generated input changes**

Run:

```powershell
git add design/design.json build.gradle.kts cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql build/cap4k/plan.json cap4k-reference-content-studio-application/src/main/kotlin cap4k-reference-content-studio-domain/src/main/kotlin cap4k-reference-content-studio-adapter/src/main/kotlin
git commit -m "feat: generate paid publication surfaces"
```

## Task 2: Implement PaidPublicationTask Domain Behavior

**Files:**

- Create: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/paid_publication_task/PaidPublicationTaskBehavior.kt`
- Create: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/paid_publication_task/factory/PaidPublicationTaskFactory.kt`
- Create: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/paid_publication_task/PaidPublicationTaskBehaviorTest.kt`
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/factory/ContentFactory.kt`
- Modify: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/factory/ContentFactoryTest.kt`
- Modify: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/ContentBehaviorTest.kt`

- [ ] **Step 1: Write domain behavior tests**

Create `PaidPublicationTaskBehaviorTest.kt` with these tests:

```kotlin
class PaidPublicationTaskBehaviorTest {

    @Test
    fun `record saga start is idempotent for same saga id`() {
        val task = newTask()
        val now = LocalDateTime.parse("2026-05-17T10:00:00")

        task.recordSagaStarted("saga-1", now)
        task.recordSagaStarted("saga-1", now.plusMinutes(1))

        assertEquals("saga-1", task.publicationSagaId)
        assertEquals(PaidPublicationStatus.RUNNING, task.paidPublicationStatus)
        assertEquals(now, task.startedAt)
    }

    @Test
    fun `record payout hold reservation and release are idempotent`() {
        val task = newTask()

        task.recordPayoutHoldReserved("hold-1")
        task.recordPayoutHoldReserved("hold-1")
        task.recordPayoutHoldReleased()
        task.recordPayoutHoldReleased()

        assertEquals("hold-1", task.payoutHoldId)
        assertEquals(PayoutHoldStatus.RELEASED, task.payoutHoldStatus)
    }

    @Test
    fun `activated entitlement cannot be cancelled automatically`() {
        val task = newTask()

        task.recordEntitlementPlanCreated("plan-1")
        task.recordEntitlementPlanActivated()

        assertThrows<IllegalStateException> {
            task.recordEntitlementPlanCancelled()
        }
    }

    @Test
    fun `published task can require operator repair`() {
        val task = newTask()
        val publishedAt = LocalDateTime.parse("2026-05-17T11:00:00")
        val failedAt = publishedAt.plusMinutes(1)

        task.markPublished(publishedAt)
        task.markRequiresOperatorRepair("activation failed", failedAt)

        assertEquals(PaidPublicationStatus.REQUIRES_OPERATOR_REPAIR, task.paidPublicationStatus)
        assertEquals("activation failed", task.failedReason)
        assertEquals(failedAt, task.failedAt)
    }
}
```

Add a private `newTask()` factory helper that constructs the generated `PaidPublicationTask` with `PENDING`, `NONE`, and `NONE` enum values.

- [ ] **Step 2: Run tests to verify failure**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*PaidPublicationTaskBehaviorTest"
```

Expected: compilation fails because behavior functions and factory do not exist.

- [ ] **Step 3: Implement factory**

Create `PaidPublicationTaskFactory.kt`:

```kotlin
@Aggregate(
    aggregate = "PaidPublicationTask",
    name = "PaidPublicationTaskFactory",
    description = "paid publication task factory"
)
class PaidPublicationTaskFactory :
    AggregateFactory<PaidPublicationTaskFactory.Payload, PaidPublicationTask> {

    override fun create(entityPayload: Payload): PaidPublicationTask =
        PaidPublicationTask(
            contentId = entityPayload.contentId,
            paidPublicationStatus = PaidPublicationStatus.PENDING,
            publicationSagaId = null,
            payoutHoldStatus = PayoutHoldStatus.NONE,
            payoutHoldId = null,
            entitlementPlanStatus = EntitlementPlanStatus.NONE,
            entitlementPlanId = null,
            startedAt = null,
            publishedAt = null,
            completedAt = null,
            failedAt = null,
            failedReason = null,
            dbCreatedAt = entityPayload.now,
            dbUpdatedAt = entityPayload.now,
        )

    data class Payload(
        val contentId: UUID,
        val now: LocalDateTime,
    ) : AggregatePayload<PaidPublicationTask>
}
```

- [ ] **Step 4: Implement behavior**

Create `PaidPublicationTaskBehavior.kt` with methods:

```kotlin
fun PaidPublicationTask.recordSagaStarted(sagaId: String, now: LocalDateTime)
fun PaidPublicationTask.recordPayoutHoldReserved(payoutHoldId: String)
fun PaidPublicationTask.recordPayoutHoldReleased()
fun PaidPublicationTask.recordEntitlementPlanCreated(entitlementPlanId: String)
fun PaidPublicationTask.recordEntitlementPlanCancelled()
fun PaidPublicationTask.recordEntitlementPlanActivated()
fun PaidPublicationTask.markPublished(publishedAt: LocalDateTime)
fun PaidPublicationTask.markFailed(reason: String, failedAt: LocalDateTime)
fun PaidPublicationTask.markRequiresOperatorRepair(reason: String, failedAt: LocalDateTime)
```

Required rules:

- `recordSagaStarted` requires non-blank saga ID; if same ID already recorded, return; if different ID recorded, fail.
- `recordPayoutHoldReserved` returns when same hold is already reserved or released; fails when a different hold ID is already present.
- `recordPayoutHoldReleased` returns when status is `NONE` or `RELEASED`; fails when status is `CAPTURED`.
- `recordEntitlementPlanCreated` returns when same plan is already created, activated, or cancelled; fails when a different plan ID exists.
- `recordEntitlementPlanCancelled` returns when status is `NONE` or `CANCELLED`; fails when status is `ACTIVATED`.
- `recordEntitlementPlanActivated` requires status `CREATED` or `ACTIVATED`; returns when already activated.
- `markPublished` returns when already `PUBLISHED`; fails when status is `FAILED`.
- `markRequiresOperatorRepair` always sets status to `REQUIRES_OPERATOR_REPAIR` and records `failedReason`.

- [ ] **Step 5: Update content factory tests**

Remove assertions for release windows. Add an assertion that paid content can be created through factory payload with `releasePolicy = ReleasePolicy.PAID`.

- [ ] **Step 6: Run domain tests**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test
```

Expected: PASS.

- [ ] **Step 7: Commit domain behavior**

Run:

```powershell
git add cap4k-reference-content-studio-domain
git commit -m "feat: add paid publication task domain behavior"
```

## Task 3: Add Paid Publication External Capability Clients

**Files:**

- Create or complete generated shell files under `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/distributed/clients/paid/publication/`
- Create: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/application/distributed/clients/paid/publication/FakePaidPublicationCliHandlers.kt`

- [ ] **Step 1: Verify generated client request/response shells**

Generation from Task 1 must produce these client objects. If any object is missing, stop this task and fix `design/design.json`; do not handwrite a generated surface.

`ReserveCreatorPayoutHoldCli` shape:

```kotlin
object ReserveCreatorPayoutHoldCli {
    data class Request(
        val paidPublicationTaskId: UUID,
    ) : RequestParam<Response>

    data class Response(
        val payoutHoldId: String,
    )
}
```

Required generated client response shapes:

```kotlin
object ReleaseCreatorPayoutHoldCli {
    data class Request(
        val paidPublicationTaskId: UUID,
    ) : RequestParam<Response>

    data class Response(
        val released: Boolean,
    )
}
```

```kotlin
object CreateAccessEntitlementPlanCli {
    data class Request(
        val paidPublicationTaskId: UUID,
    ) : RequestParam<Response>

    data class Response(
        val entitlementPlanId: String,
    )
}
```

```kotlin
object CancelAccessEntitlementPlanCli {
    data class Request(
        val paidPublicationTaskId: UUID,
    ) : RequestParam<Response>

    data class Response(
        val cancelled: Boolean,
    )
}
```

```kotlin
object ActivateAccessEntitlementPlanCli {
    data class Request(
        val paidPublicationTaskId: UUID,
    ) : RequestParam<Response>

    data class Response(
        val activated: Boolean,
    )
}
```

- [ ] **Step 2: Create fake handlers**

Create `FakePaidPublicationCliHandlers.kt`:

```kotlin
@Service
class ReserveCreatorPayoutHoldCliHandler :
    RequestHandler<ReserveCreatorPayoutHoldCli.Request, ReserveCreatorPayoutHoldCli.Response> {
    override fun exec(request: ReserveCreatorPayoutHoldCli.Request) =
        ReserveCreatorPayoutHoldCli.Response(
            payoutHoldId = "hold-${request.paidPublicationTaskId}",
        )
}
```

Add `ReleaseCreatorPayoutHoldCliHandler`:

```kotlin
@Service
class ReleaseCreatorPayoutHoldCliHandler :
    RequestHandler<ReleaseCreatorPayoutHoldCli.Request, ReleaseCreatorPayoutHoldCli.Response> {
    override fun exec(request: ReleaseCreatorPayoutHoldCli.Request) =
        ReleaseCreatorPayoutHoldCli.Response(released = true)
}
```

Add `CreateAccessEntitlementPlanCliHandler`:

```kotlin
@Service
class CreateAccessEntitlementPlanCliHandler :
    RequestHandler<CreateAccessEntitlementPlanCli.Request, CreateAccessEntitlementPlanCli.Response> {
    override fun exec(request: CreateAccessEntitlementPlanCli.Request) =
        CreateAccessEntitlementPlanCli.Response(
            entitlementPlanId = "plan-${request.paidPublicationTaskId}",
        )
}
```

Add `CancelAccessEntitlementPlanCliHandler`:

```kotlin
@Service
class CancelAccessEntitlementPlanCliHandler :
    RequestHandler<CancelAccessEntitlementPlanCli.Request, CancelAccessEntitlementPlanCli.Response> {
    override fun exec(request: CancelAccessEntitlementPlanCli.Request) =
        CancelAccessEntitlementPlanCli.Response(cancelled = true)
}
```

Add `ActivateAccessEntitlementPlanCliHandler`:

```kotlin
@Service
class ActivateAccessEntitlementPlanCliHandler(
    @Value("\${contentStudio.fakeEntitlement.failActivation:false}")
    private val failActivation: Boolean,
) : RequestHandler<ActivateAccessEntitlementPlanCli.Request, ActivateAccessEntitlementPlanCli.Response> {
    override fun exec(request: ActivateAccessEntitlementPlanCli.Request): ActivateAccessEntitlementPlanCli.Response {
        if (failActivation) {
            throw IllegalStateException("Fake entitlement activation failed.")
        }
        return ActivateAccessEntitlementPlanCli.Response(activated = true)
    }
}
```

- [ ] **Step 3: Add activation-failure test hook**

Confirm `ActivateAccessEntitlementPlanCliHandler` injects:

```kotlin
@Value("\${contentStudio.fakeEntitlement.failActivation:false}")
private val failActivation: Boolean
```

and fails deterministically:

```kotlin
if (failActivation) {
    throw IllegalStateException("Fake entitlement activation failed.")
}
```

This property is only for smoke tests and must not appear in domain code.

- [ ] **Step 4: Compile adapter**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-adapter:compileKotlin
```

Expected: PASS.

- [ ] **Step 5: Commit client handlers**

Run:

```powershell
git add cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/distributed/clients/paid/publication cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/application/distributed/clients/paid/publication
git commit -m "feat: add paid publication fake clients"
```

## Task 4: Implement Paid Publication Commands

**Files:**

- Create or complete files under `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/paid/publication/`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/CreatePaidContentDraftCmd.kt`
- Create or complete: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/paid/publication/PublishPaidPublicationContentCmd.kt`

- [ ] **Step 1: Implement CreatePaidContentDraftCmd**

Mirror `CreateContentDraftCmd`, but create `Content` with `ReleasePolicy.PAID`.

Handler shape:

```kotlin
val content = Mediator.factories.create(
    ContentFactory.Payload(
        title = request.title,
        body = request.body,
        mediaSourceKey = request.mediaSourceKey,
        reviewStatus = ReviewStatus.PENDING,
        contentStatus = ContentStatus.DRAFT,
        releasePolicy = ReleasePolicy.PAID,
    )
)
Mediator.uow.save()
return Response(contentId = content.id)
```

- [ ] **Step 2: Implement TryStartPaidPublicationCmd**

Handler rules:

- load `Content` by `contentId`;
- return `Response(taskId = existing.id, started = false)` if a task already has `publicationSagaId`;
- validate `content.releasePolicy == ReleasePolicy.PAID`;
- validate content is not already published;
- load media-processing task by `contentId` and validate `processingStatus == SUCCEEDED`;
- find or create `PaidPublicationTask`;
- call `Mediator.requests.async(PaidPublicationSaga.Request(paidPublicationTaskId = task.id))`;
- record Saga ID on task;
- save through UoW.

Core implementation:

```kotlin
val sagaId = Mediator.requests.async(
    PaidPublicationSaga.Request(paidPublicationTaskId = task.id)
)
task.recordSagaStarted(sagaId, LocalDateTime.now())
Mediator.uow.save()
return Response(taskId = task.id, started = true)
```

- [ ] **Step 3: Implement forward commands**

`ReserveCreatorPayoutHoldCmd`:

```kotlin
if (task.payoutHoldStatus == PayoutHoldStatus.RESERVED || task.payoutHoldStatus == PayoutHoldStatus.RELEASED) {
    return Response(reserved = false)
}
val response = Mediator.requests.send(
    ReserveCreatorPayoutHoldCli.Request(request.paidPublicationTaskId)
)
task.recordPayoutHoldReserved(response.payoutHoldId)
Mediator.uow.save()
return Response(reserved = true)
```

`CreateAccessEntitlementPlanCmd`:

```kotlin
if (task.entitlementPlanStatus != EntitlementPlanStatus.NONE) {
    return Response(created = false)
}
val response = Mediator.requests.send(
    CreateAccessEntitlementPlanCli.Request(request.paidPublicationTaskId)
)
task.recordEntitlementPlanCreated(response.entitlementPlanId)
Mediator.uow.save()
return Response(created = true)
```

`ActivateAccessEntitlementPlanCmd`:

```kotlin
if (task.entitlementPlanStatus == EntitlementPlanStatus.ACTIVATED) {
    return Response(activated = false)
}
Mediator.requests.send(
    ActivateAccessEntitlementPlanCli.Request(request.paidPublicationTaskId)
)
task.recordEntitlementPlanActivated()
Mediator.uow.save()
return Response(activated = true)
```

- [ ] **Step 4: Implement paid publish command**

`PublishPaidPublicationContentCmd` loads `PaidPublicationTask`, sends the existing content command, then records business task publication state:

```kotlin
val response = Mediator.cmd.send(
    PublishContentCmd.Request(
        contentId = task.contentId,
        publishedAt = LocalDateTime.now(),
        releaseReadinessSatisfied = true,
    )
)
if (response.published) {
    task.markPublished(LocalDateTime.now())
    Mediator.uow.save()
}
return Response(published = response.published)
```

Use a single `now` variable so `PublishContentCmd` and `task.markPublished` receive the same timestamp.

- [ ] **Step 5: Implement compensation commands**

`CancelEntitlementPlanIfCreatedCmd`:

```kotlin
if (task.entitlementPlanStatus == EntitlementPlanStatus.NONE ||
    task.entitlementPlanStatus == EntitlementPlanStatus.CANCELLED
) {
    return Response(cancelled = false)
}
if (task.entitlementPlanStatus == EntitlementPlanStatus.ACTIVATED) {
    task.markRequiresOperatorRepair("Entitlement plan is activated and cannot be cancelled automatically.", LocalDateTime.now())
    Mediator.uow.save()
    return Response(cancelled = false)
}
Mediator.requests.send(CancelAccessEntitlementPlanCli.Request(request.paidPublicationTaskId))
task.recordEntitlementPlanCancelled()
Mediator.uow.save()
return Response(cancelled = true)
```

`ReleasePayoutHoldIfReservedCmd`:

```kotlin
if (task.payoutHoldStatus == PayoutHoldStatus.NONE ||
    task.payoutHoldStatus == PayoutHoldStatus.RELEASED
) {
    return Response(released = false)
}
Mediator.requests.send(ReleaseCreatorPayoutHoldCli.Request(request.paidPublicationTaskId))
task.recordPayoutHoldReleased()
Mediator.uow.save()
return Response(released = true)
```

`MarkPaidPublicationFailedCmd`:

```kotlin
if (task.paidPublicationStatus == PaidPublicationStatus.PUBLISHED) {
    task.markRequiresOperatorRepair(request.failedReason, LocalDateTime.now())
} else {
    task.markFailed(request.failedReason, LocalDateTime.now())
}
Mediator.uow.save()
return Response
```

- [ ] **Step 6: Compile application**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-application:compileKotlin
```

Expected: PASS.

- [ ] **Step 7: Commit commands**

Run:

```powershell
git add cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands
git commit -m "feat: implement paid publication commands"
```

## Task 5: Implement PaidPublicationSaga

**Files:**

- Create: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/sagas/paid/publication/PaidPublicationSaga.kt`

- [ ] **Step 1: Write Saga**

Create `PaidPublicationSaga.kt` with the process constants from the spec and this request:

```kotlin
// Current cap4k Saga process caching makes automatic forward retry unsafe after compensation.
// cap4k issue #58 tracks first-class compensation runtime support.
@Retry(
    retryTimes = 1,
    retryIntervals = [1],
    expireAfter = 1440
)
data class Request(
    val paidPublicationTaskId: UUID,
) : SagaParam<Response>
```

Forward sequence:

```kotlin
execProcess(PROCESS_RESERVE_PAYOUT_HOLD, ReserveCreatorPayoutHoldCmd.Request(request.paidPublicationTaskId))
execProcess(PROCESS_CREATE_ENTITLEMENT_PLAN, CreateAccessEntitlementPlanCmd.Request(request.paidPublicationTaskId))
            execProcess(PROCESS_PUBLISH_CONTENT, PublishPaidPublicationContentCmd.Request(request.paidPublicationTaskId))
execProcess(PROCESS_ACTIVATE_ENTITLEMENT_PLAN, ActivateAccessEntitlementPlanCmd.Request(request.paidPublicationTaskId))
```

`PublishPaidPublicationContentCmd` is included in Task 1 design input. Do not handwrite this surface without the design entry.

- [ ] **Step 2: Write compensation block**

Use this failure handling shape. Forward success returns `published = true`; a forward failure with successful compensation returns `published = false`; only compensation failure rethrows the primary failure.

```kotlin
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
```

`compensateBestEffort` must return the compensation failures and must run every compensation command, including `MarkPaidPublicationFailedCmd` even when earlier compensation failed:

```kotlin
CancelEntitlementPlanIfCreatedCmd.Request(request.paidPublicationTaskId, primary.message ?: "Paid publication saga failed.")
ReleasePayoutHoldIfReservedCmd.Request(request.paidPublicationTaskId, primary.message ?: "Paid publication saga failed.")
MarkPaidPublicationFailedCmd.Request(request.paidPublicationTaskId, buildFailureReason(primary, compensationFailures))
```

Add suppressed compensation failures to `primary` before rethrowing only when compensation fails. Do not automatically retry the forward path after compensation; the current cap4k runtime can return cached `EXECUTED` process results and reuse old compensation process records.

- [ ] **Step 3: Compile Saga**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-application:compileKotlin
```

Expected: PASS.

- [ ] **Step 4: Commit Saga**

Run:

```powershell
git add cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/sagas cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/paid/publication
git commit -m "feat: add paid publication saga"
```

## Task 6: Wire Paid Path Into HTTP And Media Processing Flow

**Files:**

- Create: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/http/AdvancedPaidPublicationController.kt`
- Create or complete: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/portal/api/payload/content/workflow/CreatePaidContentDraftPayload.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/domain/media_processing_task/MediaProcessingSucceededDomainEventSubscriber.kt`
- Create: `cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/ContentStudioPaidPublicationSagaHttpSmokeTest.kt`

- [ ] **Step 1: Add controller**

Create endpoint:

```kotlin
@RestController
class AdvancedPaidPublicationController {

    @PostMapping("/advanced/contents/paid")
    fun createPaid(@RequestBody request: CreatePaidContentDraftPayload.Request): CreatePaidContentDraftPayload.Response {
        val response = Mediator.cmd.send(
            CreatePaidContentDraftCmd.Request(
                title = request.title,
                body = request.body,
                mediaSourceKey = request.mediaSourceKey,
            )
        )
        return CreatePaidContentDraftPayload.Response(contentId = response.contentId)
    }
}
```

- [ ] **Step 2: Update media-processing subscriber**

Remove `openGatedPublicationReleaseReadiness`.

Add a second listener:

```kotlin
@EventListener(MediaProcessingSucceededDomainEvent::class)
fun startPaidPublication(event: MediaProcessingSucceededDomainEvent) {
    Mediator.cmd.send(
        TryStartPaidPublicationCmd.Request(contentId = event.contentId)
    )
}
```

Keep `publishImmediateContent` unchanged. For paid content, `PublishContentCmd` must return `published = false` until the Saga calls it with `releaseReadinessSatisfied = true`.

- [ ] **Step 3: Add success smoke test**

Create `ContentStudioPaidPublicationSagaHttpSmokeTest.kt`. It should:

1. POST `/advanced/contents/paid`;
2. submit review;
3. approve review;
4. wait for media task `SUBMITTED`;
5. send media-processing callback;
6. wait for content `PUBLISHED`;
7. assert `__saga_process` contains:

```kotlin
assertThat(sagaProcessCodes())
    .contains(
        "reserve-payout-hold",
        "create-entitlement-plan",
        "publish-content",
        "activate-entitlement-plan",
    )
```

8. assert `paid_publication_task` has `paid_publication_status = 2`, `payout_hold_status = 1`, and `entitlement_plan_status = 2`.

- [ ] **Step 4: Add manual repair smoke test**

Create a second test class or method with:

```kotlin
@SpringBootTest(
    classes = [ContentStudioApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.datasource.url=jdbc:h2:mem:content-studio-paid-failure-test;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "server.port=0",
        "contentStudio.fakeEntitlement.failActivation=true",
    ],
)
```

Run the same paid path and assert `paid_publication_task.paid_publication_status = 4` after activation failure. Also assert content is `PUBLISHED`; this proves automatic rollback is not pretended after publication succeeds.

- [ ] **Step 5: Run start tests**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-start:test --tests "*ContentStudioPaidPublicationSagaHttpSmokeTest"
```

Expected: PASS.

- [ ] **Step 6: Commit paid HTTP path**

Run:

```powershell
git add cap4k-reference-content-studio-adapter/src/main/kotlin cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/domain/media_processing_task cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start
git commit -m "feat: wire paid publication saga path"
```

## Task 7: Delete PublicationReleaseReadiness Feature

**Files:**

- Delete all files listed in the deletion section of this plan.
- Modify any imports that referenced deleted release-readiness types.

- [ ] **Step 1: Delete old handwritten files**

Run:

```powershell
rg --files | rg "publication_release_readiness|release/readiness|PublicationReleaseSaga|AdvancedReleaseReadiness|advanced-release-readiness|PublicationReleaseContinuationJob"
```

Delete every returned old-feature file, excluding `docs/superpowers/specs/2026-05-17-paid-publication-saga-design.md`.

- [ ] **Step 2: Delete old checked-in release-readiness surfaces**

Run:

```powershell
rg -n "publication_release_readiness|PublicationReleaseReadiness" design cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql cap4k-reference-content-studio-domain/src/main/kotlin cap4k-reference-content-studio-application/src/main/kotlin cap4k-reference-content-studio-adapter/src/main/kotlin http README.md README.en.md
```

Use the matches to remove stale checked-in release-readiness surfaces and references.

- [ ] **Step 3: Remove stale references**

Run:

```powershell
rg -n "PublicationReleaseReadiness|PublicationReleaseSaga|CreateGated|ReleaseReadiness|releaseWindow|GATED" .
```

Expected remaining references only in:

```text
docs/superpowers/specs/2026-05-17-paid-publication-saga-design.md
```

If references appear in source, checked-in generated surfaces, HTTP files, README, OpenAPI, or analysis output, remove or regenerate them in this task.

- [ ] **Step 4: Compile all modules**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:compileKotlin :cap4k-reference-content-studio-application:compileKotlin :cap4k-reference-content-studio-adapter:compileKotlin :cap4k-reference-content-studio-start:compileKotlin
```

Expected: PASS.

- [ ] **Step 5: Commit deletion**

Run:

```powershell
git add -A
git commit -m "refactor: remove release readiness saga path"
```

## Task 8: Update Docs, HTTP Files, OpenAPI, And Analysis

**Files:**

- Modify: `README.md`
- Modify: `README.zh-CN.md`
- Create: `http/paid-publication.http`
- Delete: `http/advanced-release-readiness.http`
- Regenerate: `openapi/content-studio-openapi.json`
- Regenerate: `analysis/flows/**`

- [ ] **Step 1: Update README advanced section**

Replace the gated release paragraph with:

```markdown
The advanced path is opt-in paid publication. It demonstrates cap4k Saga as a
compensation-oriented process, not as a waiting mechanism. The Saga reserves a
creator payout hold, creates an access entitlement plan, publishes content, and
activates the entitlement plan. If a later step fails, idempotent compensation
commands release or cancel earlier side effects where business rules allow it.
```

Add the same meaning in `README.zh-CN.md`:

```markdown
高级路径是显式 opt-in 的付费内容发布。它演示的是补偿型 Saga，而不是等待型 Saga。
Saga 会预留创作者收益、创建访问权益计划、发布内容并激活权益计划；如果后续步骤失败，
会通过幂等补偿命令释放或取消前面已经完成且业务允许撤销的副作用。
```

- [ ] **Step 2: Add paid HTTP script**

Create `http/paid-publication.http`:

```http
@baseUrl = http://localhost:8080
@contentId =

### Create paid content draft.
POST {{baseUrl}}/advanced/contents/paid
Content-Type: application/json
Accept: application/json

{
  "title": "Paid sample content",
  "body": "Sample paid content body",
  "mediaSourceKey": "sample-paid-media-source-key"
}

### Then run review.http submit and approve with the created contentId.
### Then run query.http to copy task.externalTaskId.
### Then run media-processing.http with the copied externalTaskId.
### Re-run query.http after callback; paid publication Saga publishes the content.
```

- [ ] **Step 3: Regenerate OpenAPI**

Start the app:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-start:bootRun
```

In another shell, fetch:

```powershell
Invoke-WebRequest http://localhost:8080/v3/api-docs -OutFile openapi/content-studio-openapi.json
```

Expected: `/advanced/contents/paid` exists and old `/advanced/contents/gated` paths are absent.

- [ ] **Step 4: Regenerate analysis**

Run:

```powershell
.\gradlew.bat cap4kAnalysisPlan cap4kAnalysisGenerate
```

Expected: `analysis/flows/index.json` and flow files update. Old `AdvancedReleaseReadinessCon` files are gone; new paid-publication paths appear if analysis identifies them.

- [ ] **Step 5: Commit docs and generated evidence**

Run:

```powershell
git add README.md README.zh-CN.md http openapi/content-studio-openapi.json analysis/flows
git commit -m "docs: document paid publication saga path"
```

## Task 9: Final Verification

**Files:**

- No planned source edits. Fix only if verification exposes a real defect introduced by earlier tasks.

- [ ] **Step 1: Run full tests**

Run:

```powershell
.\gradlew.bat test
```

Expected: PASS.

- [ ] **Step 2: Run generation drift check**

Run:

```powershell
.\gradlew.bat cap4kPlan cap4kGenerate
git diff --exit-code -- build/cap4k/plan.json
```

Expected: Gradle tasks PASS and `git diff --exit-code` exits 0.

- [ ] **Step 3: Run stale-reference checks**

Run:

```powershell
rg -n "PublicationReleaseReadiness|PublicationReleaseSaga|CreateGated|ReleaseReadiness|releaseWindow|GATED" . --glob "!docs/superpowers/specs/2026-05-17-paid-publication-saga-design.md"
```

Expected: no matches.

Run:

```powershell
rg -n "PaidPublicationSaga|paid_publication_task|CreatePaidContentDraft|/advanced/contents/paid" .
```

Expected: matches in source, tests, README, HTTP, schema, design, checked-in generated surfaces, and analysis/openapi evidence.

- [ ] **Step 4: Run final git checks**

Run:

```powershell
git status --short
git log --oneline -5
```

Expected: clean working tree after all task commits. Latest commits should correspond to the task commits in this plan.

- [ ] **Step 5: Request review**

Summarize:

- paid publication Saga path implemented;
- old release readiness path removed;
- full test and generation evidence;
- cap4k issue #58 remains the upstream enhancement follow-up.
