# Content Studio Modeling Benchmark Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> This plan is temporary scaffolding. Delete it before the final master-ready result.

**Goal:** Rebuild `cap4k-reference-content-studio` into a clean cap4k modeling benchmark without leaving spec/plan artifacts in the final branch.

**Architecture:** Keep immediate publication as the conservative default path and paid publication as the explicit advanced path. Use `design/design.json` and schema as generation inputs, use aggregate behavior for invariants, use commands as write boundaries, route controllers/subscribers/jobs/Saga through commands, and use analysis output only as evidence.

**Tech Stack:** Kotlin, Spring Boot, Gradle, cap4k pipeline, JUnit 5, AssertJ, H2, cap4k domain/application/adapter/start modules.

**Execution Note:** The user does not require a clean git log. Do not add commit steps. Use TDD red/green checkpoints, `git diff --check`, and final cleanup instead.

---

## File Map

Formal project artifacts to keep:

- Create: `docs/modeling.md`
- Modify: `README.md`
- Modify: `README.en.md` if the English README has matching guidance
- Modify: `design/design.json`
- Modify: `http/media-processing.http`
- Modify: `http/query.http`
- Modify: `cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql` only if field comments or schema carrier facts need clarification
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/ContentBehavior.kt`
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/factory/ContentFactory.kt`
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/MediaProcessingTaskBehavior.kt`
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/values/MediaProcessingResultSnapshot.kt`
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/paid_publication_task/PaidPublicationTaskBehavior.kt`
- Create: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/services/paid/publication/PaidPublicationEligibilityService.kt`
- Modify: paid publication command handlers under `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/paid/publication/`
- Modify: media processing command handlers under `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/media/processing/`
- Modify: content subscribers under `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/domain/content/`
- Modify: integration subscriber under `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/integration/`
- Modify: query handlers under `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/application/queries/`
- Modify: HTTP controllers under `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/http/`

Tests to keep, but clean up after implementation:

- Modify: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/factory/ContentFactoryTest.kt`
- Modify: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/ContentBehaviorTest.kt`
- Modify: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/values/MediaProcessingResultSnapshotTest.kt`
- Modify: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/MediaProcessingTaskBehaviorTest.kt`
- Modify: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/paid_publication_task/PaidPublicationTaskBehaviorTest.kt`
- Create: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/services/paid/publication/PaidPublicationEligibilityServiceTest.kt`
- Modify: `cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/ContentStudioHappyPathHttpSmokeTest.kt`
- Modify: `cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/ContentStudioPaidPublicationSagaSmokeTest.kt`
- Create or modify: a contract test for design JSON / generated integration event surfaces under `cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/`

Temporary artifacts to keep for human review:

- `docs/superpowers/specs/2026-05-19-content-studio-modeling-benchmark-design.md`
- `docs/superpowers/plans/2026-05-19-content-studio-modeling-benchmark.md`

---

## Task 1: Lock Design JSON Contract Before Generation

**Files:**

- Modify: `design/design.json`
- Test: `cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/ContentStudioDesignContractTest.kt`

- [ ] **Step 1: Write failing contract tests**

Create or extend `ContentStudioDesignContractTest.kt` with tests that read `design/design.json` and assert:

```kotlin
@Test
fun `design uses fact-style content media processing event`() {
    val entries = designEntries()
    val event = entries.single { it.required("tag").asText() == "domain_event" && it.required("name").asText() == "ContentRequiresMediaProcessing" }

    assertThat(event.required("package").asText()).isEqualTo("content")
    assertThat(event.required("requestFields").map { it.required("name").asText() })
        .containsExactly("contentId", "mediaSourceKey")
}

@Test
fun `media processing callback is a completed external fact`() {
    val event = designEntries().single { it.required("tag").asText() == "integration_event" && it.required("name").asText() == "MediaProcessingCallback" }

    assertThat(event.required("role").asText()).isEqualTo("inbound")
    assertThat(event.required("eventName").asText()).isEqualTo("cap4k.reference.contentstudio.media-processing.completed")
    assertThat(event.required("requestFields").map { it.required("name").asText() })
        .contains("externalTaskId", "status", "assetSha256", "assetLocation", "completedAt")
}

@Test
fun `design publishes content published as outbound integration event`() {
    val event = designEntries().single { it.required("tag").asText() == "integration_event" && it.required("name").asText() == "ContentPublished" }

    assertThat(event.required("role").asText()).isEqualTo("outbound")
    assertThat(event.required("eventName").asText()).isEqualTo("cap4k.reference.contentstudio.content.published")
    assertThat(event.required("requestFields").map { it.required("name").asText() })
        .containsExactly("contentId", "releasePolicy", "publishedAt")
}
```

- [ ] **Step 2: Run tests to verify RED**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-start:test --tests "*ContentStudioDesignContractTest"
```

Expected: FAIL because `ContentRequiresMediaProcessing` and outbound `ContentPublished` are not yet in `design/design.json`, and callback event name is still `media-processing.succeeded`.

- [ ] **Step 3: Update `design/design.json`**

Change:

- Rename domain event `MediaProcessingRequested` to `ContentRequiresMediaProcessing`.
- Change inbound callback `eventName` to `cap4k.reference.contentstudio.media-processing.completed`.
- Add outbound integration event:

```json
{
  "tag": "integration_event",
  "package": "content",
  "name": "ContentPublished",
  "desc": "content published outbound event",
  "aggregates": ["Content"],
  "role": "outbound",
  "eventName": "cap4k.reference.contentstudio.content.published",
  "requestFields": [
    { "name": "contentId", "type": "java.util.UUID" },
    { "name": "releasePolicy", "type": "String" },
    { "name": "publishedAt", "type": "java.time.LocalDateTime" }
  ],
  "responseFields": []
}
```

Add `GetPaidPublicationStatus` query and payload entries with response fields:

- `contentId`
- `taskId`
- `paidPublicationStatus`
- `payoutHoldStatus`
- `entitlementPlanStatus`
- `startedAt`
- `publishedAt`
- `completedAt`
- `failedAt`
- `failedReason`

Add `releasePolicy` and `mediaReadyAt` to `GetContentDetail` query and payload response fields.

- [ ] **Step 4: Run contract tests to verify GREEN**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-start:test --tests "*ContentStudioDesignContractTest"
```

Expected: PASS.

- [ ] **Step 5: Generate planned surfaces**

Run:

```powershell
.\gradlew.bat cap4kPlan
.\gradlew.bat cap4kGenerate
```

Expected: both commands exit 0. Inspect `build/cap4k/plan.json` for generator-supported surfaces. Do not handwrite generated-capable command/query/event/payload/client skeletons that the plan can generate.

---

## Task 2: Content Factory Validation

**Files:**

- Modify: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/factory/ContentFactoryTest.kt`
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/factory/ContentFactory.kt`

- [ ] **Step 1: Write failing validation tests**

Add focused tests:

```kotlin
@Test
fun `create rejects blank title`() {
    assertThrows(IllegalArgumentException::class.java) {
        factory.create(validPayload(title = "   "))
    }
}

@Test
fun `create rejects title longer than schema limit`() {
    assertThrows(IllegalArgumentException::class.java) {
        factory.create(validPayload(title = "x".repeat(201)))
    }
}

@Test
fun `create rejects blank body`() {
    assertThrows(IllegalArgumentException::class.java) {
        factory.create(validPayload(body = "   "))
    }
}

@Test
fun `create rejects blank media source key`() {
    assertThrows(IllegalArgumentException::class.java) {
        factory.create(validPayload(mediaSourceKey = "   "))
    }
}
```

Add a thin `validPayload` helper with named override parameters in the same test file. Keep it explicit and readable.

- [ ] **Step 2: Run tests to verify RED**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*ContentFactoryTest"
```

Expected: FAIL because the factory currently accepts blank and overlong fields.

- [ ] **Step 3: Implement factory validation**

Update `ContentFactory.create` to validate and trim user-authored text:

```kotlin
override fun create(entityPayload: Payload): Content {
    val title = entityPayload.title.trim()
    val body = entityPayload.body.trim()
    val mediaSourceKey = entityPayload.mediaSourceKey.trim()

    require(title.isNotBlank()) { "Content title must not be blank." }
    require(title.length <= TITLE_MAX_LENGTH) { "Content title must be at most $TITLE_MAX_LENGTH characters." }
    require(body.isNotBlank()) { "Content body must not be blank." }
    require(mediaSourceKey.isNotBlank()) { "Content media source key must not be blank." }
    require(mediaSourceKey.length <= MEDIA_SOURCE_KEY_MAX_LENGTH) {
        "Content media source key must be at most $MEDIA_SOURCE_KEY_MAX_LENGTH characters."
    }

    return Content(
        id = entityPayload.id,
        title = title,
        body = body,
        mediaSourceKey = mediaSourceKey,
        reviewStatus = entityPayload.reviewStatus,
        contentStatus = entityPayload.contentStatus,
        releasePolicy = entityPayload.releasePolicy,
        reviewerId = entityPayload.reviewerId,
        reviewedAt = entityPayload.reviewedAt,
        publishedAt = entityPayload.publishedAt,
        mediaReadyAt = entityPayload.mediaReadyAt,
        dbCreatedAt = entityPayload.dbCreatedAt,
        dbUpdatedAt = entityPayload.dbUpdatedAt,
    )
}

companion object {
    private const val TITLE_MAX_LENGTH = 200
    private const val MEDIA_SOURCE_KEY_MAX_LENGTH = 200
}
```

- [ ] **Step 4: Run tests to verify GREEN**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*ContentFactoryTest"
```

Expected: PASS.

---

## Task 3: Content Media Requirement Event Rename

**Files:**

- Modify: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/ContentBehaviorTest.kt`
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/ContentBehavior.kt`
- Modify or generated: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/events/ContentRequiresMediaProcessingDomainEvent.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/domain/content/*RequiresMediaProcessing*Subscriber.kt`

- [ ] **Step 1: Update tests to require fact-style event**

Replace imports and assertions for `MediaProcessingRequestedDomainEvent` with:

```kotlin
val contentRequiresMediaProcessingEvent = assertInstanceOf(
    ContentRequiresMediaProcessingDomainEvent::class.java,
    domainEvents.attachedEvents[1],
)
assertEquals(content.id, contentRequiresMediaProcessingEvent.contentId)
assertEquals(content.mediaSourceKey, contentRequiresMediaProcessingEvent.mediaSourceKey)
```

- [ ] **Step 2: Run tests to verify RED**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*ContentBehaviorTest"
```

Expected: FAIL until generated event class exists and `ContentBehavior` emits it.

- [ ] **Step 3: Regenerate if the event skeleton is missing**

Run:

```powershell
.\gradlew.bat cap4kPlan
.\gradlew.bat cap4kGenerate
```

Expected: generated event class and subscriber shell are available according to `plan.json`. If generation cannot produce the rename cleanly, delete only obsolete generated-capable checked-in surfaces that are confirmed stale by `plan.json`; do not delete handwritten logic blindly.

- [ ] **Step 4: Update behavior and subscriber**

In `ContentBehavior.kt`, import and attach `ContentRequiresMediaProcessingDomainEvent` instead of `MediaProcessingRequestedDomainEvent`.

Rename the subscriber to route:

```kotlin
@EventListener(ContentRequiresMediaProcessingDomainEvent::class)
fun startMediaProcessing(event: ContentRequiresMediaProcessingDomainEvent) {
    Mediator.cmd.send(
        StartMediaProcessingCmd.Request(
            contentId = event.contentId,
            mediaSourceKey = event.mediaSourceKey,
        )
    )
}
```

- [ ] **Step 5: Run tests to verify GREEN**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*ContentBehaviorTest"
.\gradlew.bat :cap4k-reference-content-studio-application:compileKotlin
```

Expected: both commands exit 0.

---

## Task 4: Media Processing Result Value Object And Task Guard

**Files:**

- Modify: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/values/MediaProcessingResultSnapshotTest.kt`
- Modify: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/MediaProcessingTaskBehaviorTest.kt`
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/values/MediaProcessingResultSnapshot.kt`
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/MediaProcessingTaskBehavior.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/media/processing/MarkMediaProcessingSucceededCmd.kt`

- [ ] **Step 1: Write failing value-object tests**

Extend snapshot tests with `contentId`:

```kotlin
@Test
fun `creates a snapshot for a specific content id`() {
    val snapshot = snapshot()

    assertEquals(contentId, snapshot.contentId)
}

@Test
fun `blank asset location is rejected`() {
    assertThrows(IllegalArgumentException::class.java) {
        snapshot(assetLocation = "   ")
    }
}
```

Use a helper with:

```kotlin
private val contentId = UUID.fromString("00000000-0000-0000-0000-000000000201")
```

- [ ] **Step 2: Write failing task guard test**

In `MediaProcessingTaskBehaviorTest`, add:

```kotlin
@Test
fun `mark succeeded rejects snapshot for another content`() {
    val task = submittedTask()
    val snapshot = snapshot(taskId = task.id, contentId = UUID.randomUUID(), externalTaskId = task.externalTaskId!!)

    assertThrows(IllegalStateException::class.java) {
        task.markSucceeded(snapshot)
    }
}
```

- [ ] **Step 3: Run tests to verify RED**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*MediaProcessingResultSnapshotTest" --tests "*MediaProcessingTaskBehaviorTest"
```

Expected: FAIL because `MediaProcessingResultSnapshot` does not yet expose `contentId` and task behavior does not check it.

- [ ] **Step 4: Implement value object field and behavior guard**

Add `contentId: UUID` to `MediaProcessingResultSnapshot`, include it in `create` and `computeHash`, and add:

```kotlin
check(resultSnapshot.contentId == contentId) {
    "Media processing result snapshot does not belong to this content."
}
```

inside `MediaProcessingTask.markSucceeded`.

Update `MarkMediaProcessingSucceededCmd` to pass `contentId = task.contentId` when constructing the snapshot.

- [ ] **Step 5: Run tests to verify GREEN**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*MediaProcessingResultSnapshotTest" --tests "*MediaProcessingTaskBehaviorTest"
.\gradlew.bat :cap4k-reference-content-studio-application:compileKotlin
```

Expected: both commands exit 0.

---

## Task 5: Paid Publication Aggregate Invariants And Facts

**Files:**

- Modify: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/paid_publication_task/PaidPublicationTaskBehaviorTest.kt`
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/paid_publication_task/PaidPublicationTaskBehavior.kt`
- Generated or modify: paid publication domain event classes from `design/design.json`

- [ ] **Step 1: Write failing invariant tests**

Add tests that express process ordering:

```kotlin
@Test
fun `payout hold can only be reserved after saga starts`() {
    assertThrows(IllegalStateException::class.java) {
        newTask().recordPayoutHoldReserved("hold-123")
    }
}

@Test
fun `entitlement plan requires reserved payout hold`() {
    val task = runningTask()

    assertThrows(IllegalStateException::class.java) {
        task.recordEntitlementPlanCreated("plan-123")
    }
}

@Test
fun `paid publication can be marked published only after hold and plan are ready`() {
    val task = runningTask()
    val publishedAt = LocalDateTime.of(2026, 5, 17, 11, 0)

    assertThrows(IllegalStateException::class.java) {
        task.markPublished(publishedAt)
    }
}

@Test
fun `entitlement activation completes published paid publication`() {
    val task = runningTaskWithHoldAndPlan()
    val publishedAt = LocalDateTime.of(2026, 5, 17, 11, 0)

    task.markPublished(publishedAt)
    task.recordEntitlementPlanActivated(publishedAt.plusMinutes(5))

    assertEquals(PaidPublicationStatus.PUBLISHED, task.paidPublicationStatus)
    assertEquals(EntitlementPlanStatus.ACTIVATED, task.entitlementPlanStatus)
    assertEquals(publishedAt.plusMinutes(5), task.completedAt)
}
```

Adjust helper names to stay explicit: `runningTask()` should call `recordSagaStarted`, and `runningTaskWithHoldAndPlan()` should reserve hold and create plan.

- [ ] **Step 2: Run tests to verify RED**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*PaidPublicationTaskBehaviorTest"
```

Expected: FAIL because current behavior allows several transitions before prerequisites.

- [ ] **Step 3: Implement aggregate guards and events**

Update behavior:

```kotlin
fun PaidPublicationTask.recordPayoutHoldReserved(payoutHoldId: String) {
    check(paidPublicationStatus == PaidPublicationStatus.RUNNING) {
        "Cannot reserve payout hold before paid publication is running."
    }
    // keep existing idempotency checks
}
```

Apply equivalent checks for entitlement plan creation, published marking, activation, failure, and repair. Change activation signature to:

```kotlin
fun PaidPublicationTask.recordEntitlementPlanActivated(completedAt: LocalDateTime)
```

Set:

```kotlin
entitlementPlanStatus = EntitlementPlanStatus.ACTIVATED
this.completedAt = completedAt
```

Attach paid publication domain events only after generation has produced the event classes. If event generation is not available for a particular event, skip only that event and document the unsupported-generation reason in implementation notes.

- [ ] **Step 4: Update command calls for changed signatures**

Update `ActivateAccessEntitlementPlanCmd` to call:

```kotlin
task.recordEntitlementPlanActivated(LocalDateTime.now())
```

Update other paid commands to expect aggregate behavior as the final invariant authority. Keep command-level no-op guards only when they express idempotent orchestration results.

- [ ] **Step 5: Run tests to verify GREEN**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*PaidPublicationTaskBehaviorTest"
.\gradlew.bat :cap4k-reference-content-studio-application:compileKotlin
```

Expected: both commands exit 0.

---

## Task 6: Paid Publication Eligibility Domain Service

**Files:**

- Create: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/services/paid/publication/PaidPublicationEligibilityService.kt`
- Create: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/services/paid/publication/PaidPublicationEligibilityServiceTest.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/paid/publication/TryStartPaidPublicationCmd.kt`

- [ ] **Step 1: Write failing domain service tests**

Create tests for:

```kotlin
@Test
fun `eligible when paid content is publication ready and no task exists`() {
    val result = service.decide(paidReadyContent(), existingTask = null)

    assertEquals(PaidPublicationEligibilityService.Decision.Eligible, result)
}

@Test
fun `not paid content is not eligible`() {
    val result = service.decide(immediateReadyContent(), existingTask = null)

    assertEquals(PaidPublicationEligibilityService.Decision.NotPaidContent, result)
}

@Test
fun `existing started task is already started`() {
    val result = service.decide(paidReadyContent(), existingTask = runningTask())

    assertEquals(PaidPublicationEligibilityService.Decision.AlreadyStarted, result)
}
```

- [ ] **Step 2: Run tests to verify RED**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*PaidPublicationEligibilityServiceTest"
```

Expected: FAIL because service does not exist.

- [ ] **Step 3: Implement domain service**

Create:

```kotlin
@Service
class PaidPublicationEligibilityService {
    fun decide(content: Content, existingTask: PaidPublicationTask?): Decision =
        when {
            content.releasePolicy != ReleasePolicy.PAID -> Decision.NotPaidContent
            content.contentStatus == ContentStatus.PUBLISHED -> Decision.AlreadyPublished
            !content.isReadyForPaidPublication() -> Decision.NotPublicationReady
            existingTask?.publicationSagaId != null -> Decision.AlreadyStarted
            else -> Decision.Eligible
        }

    enum class Decision {
        Eligible,
        AlreadyStarted,
        NotPaidContent,
        NotPublicationReady,
        AlreadyPublished,
    }
}
```

- [ ] **Step 4: Run service tests to verify GREEN**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*PaidPublicationEligibilityServiceTest"
```

Expected: PASS.

- [ ] **Step 5: Use service in command**

In `TryStartPaidPublicationCmd`, obtain the service through `Mediator.services` if the cap4k runtime exposes the generated service lookup for domain services. If `Mediator.services` is unavailable for this handwritten domain service, use constructor injection only for this service and document the reason in `docs/modeling.md`.

The command should branch on:

```kotlin
when (eligibilityService.decide(content, existingTask)) {
    PaidPublicationEligibilityService.Decision.Eligible -> {
        val now = LocalDateTime.now()
        val task =
            existingTask ?: Mediator.factories.create(
                PaidPublicationTaskFactory.Payload(
                    id = UUID.randomUUID(),
                    contentId = request.contentId,
                    now = now,
                )
            )
        val sagaId =
            Mediator.requests.schedule(
                PaidPublicationSaga.Request(paidPublicationTaskId = task.id),
                now.plusSeconds(1),
            )
        task.recordSagaStarted(sagaId, now)
        Mediator.uow.save()
        Response(taskId = task.id, started = true)
    }
    PaidPublicationEligibilityService.Decision.AlreadyStarted -> Response(taskId = existingTask?.id, started = false)
    else -> Response(taskId = existingTask?.id, started = false)
}
```

Do not move repository reads or UoW saves into the domain service.

- [ ] **Step 6: Compile application**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-application:compileKotlin
```

Expected: exit 0.

---

## Task 7: Query And HTTP Reading Surface

**Files:**

- Modify generated/checked-in query surface from `design/design.json`:
  - `GetContentDetailQry`
  - `GetPaidPublicationStatusQry`
- Modify: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/application/queries/content/read/GetContentDetailQryHandler.kt`
- Create or modify: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/application/queries/paid/publication/GetPaidPublicationStatusQryHandler.kt`
- Modify: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/http/QueryController.kt`
- Modify: `cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/ContentStudioHappyPathHttpSmokeTest.kt`

- [ ] **Step 1: Write failing HTTP/query assertions**

In the happy-path smoke test, after querying content detail, assert:

```kotlin
assertThat(content.required("releasePolicy").asText()).isEqualTo("IMMEDIATE")
assertThat(content.required("mediaReadyAt").isNull).isFalse()
```

In paid saga smoke test, after paid task completes, call:

```kotlin
val response = restTemplate.getForEntity("/paid-publication/$contentId", String::class.java)
assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
val paid = json(response.body)
assertThat(paid.required("paidPublicationStatus").asText()).isEqualTo("PUBLISHED")
assertThat(paid.required("entitlementPlanStatus").asText()).isEqualTo("ACTIVATED")
assertThat(paid.required("completedAt").isNull).isFalse()
```

- [ ] **Step 2: Run tests to verify RED**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-start:test --tests "*ContentStudioHappyPathHttpSmokeTest" --tests "*ContentStudioPaidPublicationSagaSmokeTest"
```

Expected: FAIL because query payloads/controllers do not yet expose the new fields and paid query route.

- [ ] **Step 3: Implement query handlers and controller route**

Update `GetContentDetailQryHandler` to map `releasePolicy` and `mediaReadyAt`.

Create `GetPaidPublicationStatusQryHandler` that uses read-only repository access:

```kotlin
val task = Mediator.repositories.findFirst(
    SPaidPublicationTask.predicate { schema -> schema.contentId.eq(request.contentId) }
)
```

Return nullable task fields if no task exists.

Add controller route:

```kotlin
@GetMapping("/paid-publication/{contentId}")
fun getPaidPublication(@PathVariable contentId: UUID): GetPaidPublicationStatusPayload.Response {
    val response = Mediator.qry.send(GetPaidPublicationStatusQry.Request(contentId = contentId))
    return GetPaidPublicationStatusPayload.Response(
        contentId = response.contentId,
        taskId = response.taskId,
        paidPublicationStatus = response.paidPublicationStatus,
        payoutHoldStatus = response.payoutHoldStatus,
        entitlementPlanStatus = response.entitlementPlanStatus,
        startedAt = response.startedAt,
        publishedAt = response.publishedAt,
        completedAt = response.completedAt,
        failedAt = response.failedAt,
        failedReason = response.failedReason,
    )
}
```

- [ ] **Step 4: Run tests to verify GREEN**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-start:test --tests "*ContentStudioHappyPathHttpSmokeTest" --tests "*ContentStudioPaidPublicationSagaSmokeTest"
```

Expected: PASS.

---

## Task 8: Inbound And Outbound Integration Events

**Files:**

- Modify: generated integration event class for `MediaProcessingCallbackIntegrationEvent`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/integration/MediaProcessingCallbackIntegrationEventSubscriber.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/domain/content/ContentPublishedDomainEventSubscriber.kt`
- Test: integration event contract/smoke tests

- [ ] **Step 1: Update failing callback tests**

In existing smoke tests, continue to use:

```kotlin
MediaProcessingCallbackIntegrationEvent.EVENT_NAME
```

Add assertion in a contract test:

```kotlin
assertThat(MediaProcessingCallbackIntegrationEvent.EVENT_NAME)
    .isEqualTo("cap4k.reference.contentstudio.media-processing.completed")
```

- [ ] **Step 2: Run tests to verify RED**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-start:test --tests "*ContentStudioDesignContractTest"
```

Expected: FAIL until regenerated integration event class has the new event name.

- [ ] **Step 3: Regenerate and update subscriber**

Run:

```powershell
.\gradlew.bat cap4kPlan
.\gradlew.bat cap4kGenerate
```

Ensure `MediaProcessingCallbackIntegrationEvent.EVENT_NAME` is `cap4k.reference.contentstudio.media-processing.completed`.

Keep subscriber logic:

```kotlin
if (event.status.uppercase() != SUCCEEDED_STATUS) {
    return
}
Mediator.cmd.send(
    MarkMediaProcessingSucceededCmd.Request(
        externalTaskId = event.externalTaskId,
        assetSha256 = event.assetSha256,
        assetLocation = event.assetLocation,
        completedAt = event.completedAt,
    )
)
```

- [ ] **Step 4: Implement outbound publication as supported by runtime**

If generation produces outbound integration event class, wire `ContentPublishedDomainEventSubscriber` to publish it using the cap4k runtime-supported integration event publication API. If no runtime publication API is exposed in this project, keep the generated outbound event contract and document in `docs/modeling.md` that this reference demonstrates outbound contract generation but not production transport delivery.

Add a contract-level test for the generated outbound event:

```kotlin
assertThat(ContentPublishedIntegrationEvent.EVENT_NAME)
    .isEqualTo("cap4k.reference.contentstudio.content.published")
```

- [ ] **Step 5: Run tests to verify GREEN**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-start:test --tests "*ContentStudioDesignContractTest" --tests "*ContentStudioHappyPathHttpSmokeTest"
```

Expected: PASS.

---

## Task 9: Documentation As The Public Reading Surface

**Files:**

- Create: `docs/modeling.md`
- Modify: `README.md`
- Modify: `README.en.md` if present content duplicates modeling explanation

- [ ] **Step 1: Draft formal `docs/modeling.md`**

Create sections:

- `Purpose`
- `Business Vocabulary`
- `Bounded Context`
- `Default Immediate Publication Flow`
- `Advanced Paid Publication Flow`
- `Aggregate Boundaries`
- `Value Object`
- `Domain Service`
- `Why Specification Is Not Used`
- `Command, Query, Repository, Factory, And UoW Boundaries`
- `Service Integration`
- `Saga`
- `Job`
- `Generation Ownership`
- `Analysis And Causal Flow`
- `Testing As Example Documentation`

Ensure it says:

```text
The analysis flow files are evidence generated from code. They are not business modeling inputs.
```

And:

```text
This project intentionally keeps aggregate specification generation disabled because no standalone reusable validation policy remains after aggregate invariants, value object rules, and the paid publication domain service are modeled.
```

- [ ] **Step 2: Update README**

Add a short link near "如何阅读这个仓库":

```markdown
如果你想按 cap4k 建模视角阅读这个参考项目，先看 [docs/modeling.md](docs/modeling.md)。
```

Keep README as an entry page, not the full modeling document.

- [ ] **Step 3: Run docs checks**

Run:

```powershell
rg -n "docs/superpowers|TODO|TBD|FIXME" README.md README.en.md docs/modeling.md
git diff --check
```

Expected: no `TODO/TBD/FIXME`, no formal docs referencing temporary spec/plan, no whitespace errors.

---

## Task 10: Full Verification And Analysis

**Files:**

- Generated: `build/cap4k/plan.json`
- Generated: `analysis/flows/*`

- [ ] **Step 1: Run generation verification**

Run:

```powershell
.\gradlew.bat cap4kPlan
.\gradlew.bat cap4kGenerate
```

Expected: both exit 0. Inspect `build/cap4k/plan.json` for unexpected `OVERWRITE` on handwritten source.

- [ ] **Step 2: Run test suite**

Run:

```powershell
.\gradlew.bat test
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run analysis generation**

Run:

```powershell
.\gradlew.bat cap4kAnalysisPlan
.\gradlew.bat cap4kAnalysisGenerate
```

Expected: both exit 0. `analysis/flows/index.json` should have non-empty flows. If analysis output is empty, stop and diagnose IR input generation instead of changing the business model.

- [ ] **Step 4: Review generated analysis output**

Run:

```powershell
Get-Content -Raw analysis\flows\index.json
```

Expected: controller and inbound integration event entries remain visible. Record any missing job/Saga flow as analysis coverage limitation unless code is actually unreachable.

---

## Task 11: Test Cleanup Pass

**Files:**

- All modified test files

- [ ] **Step 1: Remove construction noise**

Scan tests:

```powershell
rg -n "TODO|TBD|FIXME|should work|test1|helper|setup.*magic|mock" cap4k-reference-content-studio-domain\src\test cap4k-reference-content-studio-start\src\test
```

Expected: no unclear names or opaque helpers. If a helper hides business semantics, inline it or rename it to a business phrase.

- [ ] **Step 2: Keep behavior tests readable**

For every changed test file, ensure:

- test names describe business behavior
- each test has one reason to fail
- direct aggregate behavior tests do not depend on HTTP details
- smoke tests do not duplicate all domain invariant checks
- fixtures use domain vocabulary

- [ ] **Step 3: Run focused test suite after cleanup**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test
.\gradlew.bat :cap4k-reference-content-studio-start:test
```

Expected: both exit 0.

---

## Task 12: Final Review Gate

**Files:**

- Keep for review: `docs/superpowers/specs/2026-05-19-content-studio-modeling-benchmark-design.md`
- Keep for review: `docs/superpowers/plans/2026-05-19-content-studio-modeling-benchmark.md`

- [ ] **Step 1: Keep temporary process files for human audit**

Do not delete:

```text
docs/superpowers/specs/2026-05-19-content-studio-modeling-benchmark-design.md
docs/superpowers/plans/2026-05-19-content-studio-modeling-benchmark.md
```

They must remain available so the user can compare implementation against spec and plan. Delete them only after the user explicitly asks.

- [ ] **Step 2: Verify formal docs do not reference process files**

Run:

```powershell
rg -n "content-studio-modeling-benchmark-design|content-studio-modeling-benchmark.md|docs/superpowers/specs|docs/superpowers/plans" README.md README.en.md docs/modeling.md
```

Expected: no matches in formal reader-facing docs.

- [ ] **Step 3: Final checks**

Run:

```powershell
git diff --check
git status --short
```

Expected:

- no whitespace errors
- spec/plan files remain for review
- only intentional project artifacts remain changed

---

## Self-Review Notes

Spec coverage:

- Modeling document: Task 9.
- Default and advanced flows: Tasks 3, 5, 6, 7, 8, 9.
- Generation ownership: Tasks 1, 3, 8, 10.
- TDD: Every behavior-changing task starts with failing tests and RED verification.
- Test readability cleanup: Task 11.
- Temporary spec/plan retention for human audit: Task 12.

Known implementation decision points:

- Outbound integration event runtime publication may be contract-only in this reference project. If the runtime API is not obvious from generated code or dependencies, document the limitation rather than faking transport.
- Domain service lookup should prefer `Mediator.services`; if unavailable for the handwritten service, constructor injection is acceptable with explicit documentation.
- Analysis output may not show every Saga/job relationship. Treat that as analysis evidence scope, not a business modeling defect.
