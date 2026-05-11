# Advanced Authoring Examples Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extend `cap4k-reference-content-studio` so the cap4k advanced examples map to runnable, generator-friendly reference code without replacing the default happy path.

**Architecture:** Keep the existing immediate publication path as the default. Add a separate-table value-object example for media processing results, then add an opt-in gated publication line that records release readiness before publishing. Treat `docs/superpowers` and implementation-only tests as temporary working artifacts that must be removed before final merge.

**Tech Stack:** Kotlin, Gradle, Spring Boot, cap4k `Mediator`, cap4k pipeline generator, H2/JPA, JUnit 5, `.http` files, cap4k analysis plugin.

---

## Final Delivery Gates

These gates are mandatory before merging back to `master` or closing the feature worktree:

- `docs/` must be deleted from `cap4k-reference-content-studio`.
- Only teaching tests may remain. Delete temporary diagnosis tests, architecture-policing tests, and "crime scene" regression tests.
- The default `.http` happy path must still run.
- The advanced `.http` path must be opt-in and separate.
- `git status --short` must be clean except for expected commits before merge.

## Planned File Map

**Schema and generation input**

- Modify `cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql`
  - add `content.release_policy`
  - add optional release-window columns to `content`
  - add `media_processing_result_snapshot` table with `@Parent=media_processing_task;@VO;`
  - add `publication_release_readiness` table
- Modify `build.gradle.kts`
  - include `publication_release_readiness` in DB aggregate generation
  - keep `media_processing_result_snapshot` out of active aggregate generation until cap4k generates full `ValueObject<ID>` runtime code
- Modify `design/design.json`
  - add advanced commands and payloads
  - keep unsupported `value_object` and `domain_service` tags out of design JSON

**Domain**

- Create `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/values/MediaProcessingResultStatus.kt`
- Create `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/values/MediaProcessingResultSnapshot.kt`
- Modify `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/MediaProcessingTaskBehavior.kt`
- Modify generated snapshot-backed content/readiness files after running generation:
  - `cap4k-reference-content-studio-domain/src-generated/main/kotlin/.../content/Content.kt`
  - `cap4k-reference-content-studio-domain/src-generated/main/kotlin/.../content/enums/ReleasePolicy.kt`
  - `cap4k-reference-content-studio-domain/src-generated/main/kotlin/.../publication_release_readiness/PublicationReleaseReadiness.kt`
  - `cap4k-reference-content-studio-domain/src-generated/main/kotlin/.../publication_release_readiness/enums/*.kt`
- Create `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/publication_release_readiness/PublicationReleaseReadinessBehavior.kt`
- Modify `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/services/PublicationEligibilityDomainService.kt`

**Application**

- Modify `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/CreateContentDraftCmd.kt`
- Create `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/CreateGatedContentDraftCmd.kt`
- Modify `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/PublishContentCmd.kt`
- Modify `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/media/processing/MarkMediaProcessingSucceededCmd.kt`
- Create `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/ContinuePublicationAfterMediaSucceededCmd.kt`
- Create `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/release/readiness/OpenPublicationReleaseReadinessCmd.kt`
- Create `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/release/readiness/PassCopyrightReviewCmd.kt`
- Create `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/release/readiness/ConfirmManualReleaseCmd.kt`
- Create `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/release/readiness/CompletePublicationReleaseReadinessCmd.kt`
- Modify `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/domain/media_processing_task/MediaProcessingSucceededDomainEventSubscriber.kt`
- Modify callback and polling inputs:
  - `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/integration/MediaProcessingCallbackIntegrationEvent.kt`
  - `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/integration/MediaProcessingCallbackIntegrationEventSubscriber.kt`
  - `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/jobs/MediaProcessingPollingFallbackJob.kt`
  - `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/distributed/clients/media/processing/GetMediaProcessingStatusCli.kt`

**Adapter and operation surface**

- Modify `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/integration/FakeMediaProcessingCli.kt`
- Modify `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/application/distributed/clients/media/processing/GetMediaProcessingStatusCliHandler.kt`
- Create or modify generated payload files under `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/portal/api/payload/**`
- Create `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/http/AdvancedReleaseReadinessController.kt`
- Modify `http/media-processing.http`
- Create `http/advanced-release-readiness.http`

**Tests and docs**

- Modify clean teaching tests only:
  - `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/services/PublicationEligibilityDomainServiceTest.kt`
  - `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/MediaProcessingTaskBehaviorTest.kt`
  - `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/ContentBehaviorTest.kt`
- Create clean teaching tests:
  - `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/values/MediaProcessingResultSnapshotTest.kt`
  - `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/publication_release_readiness/PublicationReleaseReadinessBehaviorTest.kt`
  - `cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/ContentStudioAdvancedReleaseReadinessHttpSmokeTest.kt`
- Modify `README.zh-CN.md`
- Modify `README.md`
- Refresh `analysis/flows/**` only after compile/tests pass.
- Delete `docs/` before final merge.

---

### Task 1: Schema and Generator Input

**Files:**
- Modify: `cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql`
- Modify: `build.gradle.kts`
- Modify: `design/design.json`

- [ ] **Step 1: Update schema with release policy and advanced tables**

Modify the `content` table by adding these columns after `content_status`:

```sql
    release_policy int not null comment '@T=ReleasePolicy;@E=0:IMMEDIATE:Immediate|1:GATED:Gated;',
    release_window_opens_at timestamp,
    release_window_closes_at timestamp,
```

Add the separate-table value object after `media_processing_task`:

```sql
create table if not exists media_processing_result_snapshot (
    id varchar(64) primary key,
    media_processing_task_id uuid not null,
    external_task_id varchar(120) not null,
    result_status int not null comment '@T=MediaProcessingResultStatus;@E=0:SUCCEEDED:Succeeded|1:FAILED:Failed;',
    asset_sha256 varchar(64) not null,
    asset_location varchar(500) not null,
    completed_at timestamp not null,
    db_created_at timestamp not null,
    db_updated_at timestamp not null,
    constraint fk_media_processing_result_snapshot_task
        foreign key (media_processing_task_id) references media_processing_task(id)
);

comment on table media_processing_result_snapshot is
    'Media processing result snapshot @Parent=media_processing_task;@VO;';
```

Add the release-readiness process aggregate:

```sql
create table if not exists publication_release_readiness (
    id uuid primary key,
    content_id uuid not null,
    media_processing_task_id uuid not null,
    readiness_state int not null comment '@T=PublicationReleaseReadinessState;@E=0:WAITING:Waiting|1:READY:Ready|2:CANCELLED:Cancelled|3:EXPIRED:Expired;',
    copyright_status int not null comment '@T=CopyrightReviewStatus;@E=0:WAITING:Waiting|1:PASSED:Passed|2:REJECTED:Rejected;',
    manual_confirmation_status int not null comment '@T=ManualReleaseConfirmationStatus;@E=0:WAITING:Waiting|1:CONFIRMED:Confirmed;',
    release_window_opens_at timestamp not null,
    release_window_closes_at timestamp not null,
    ready_at timestamp,
    cancel_reason varchar(500),
    db_created_at timestamp not null,
    db_updated_at timestamp not null,
    constraint uq_publication_release_readiness_content_id unique (content_id),
    constraint fk_publication_release_readiness_content foreign key (content_id) references content(id),
    constraint fk_publication_release_readiness_task foreign key (media_processing_task_id) references media_processing_task(id)
);
```

- [ ] **Step 2: Keep value-object table out of active aggregate generation**

In `build.gradle.kts`, keep `media_processing_result_snapshot` out of `includeTables` because current cap4k aggregate generation does not yet emit full `ValueObject<ID>` runtime code.

Change:

```kotlin
includeTables.set(listOf("content", "media_processing_task"))
```

To:

```kotlin
includeTables.set(
    listOf(
        "content",
        "media_processing_task",
        "publication_release_readiness"
    )
)
```

Do not add `media_processing_result_snapshot` to this list in this slice.

- [ ] **Step 3: Add design entries for advanced commands and payloads**

Append these entries to `design/design.json` before the closing array bracket. Preserve valid JSON commas.

```json
{
  "tag": "command",
  "package": "content.workflow",
  "name": "CreateGatedContentDraft",
  "desc": "create gated content draft",
  "aggregates": ["Content"],
  "requestFields": [
    { "name": "title", "type": "String" },
    { "name": "body", "type": "String" },
    { "name": "mediaSourceKey", "type": "String" },
    { "name": "releaseWindowOpensAt", "type": "java.time.LocalDateTime" },
    { "name": "releaseWindowClosesAt", "type": "java.time.LocalDateTime" }
  ],
  "responseFields": [
    { "name": "contentId", "type": "java.util.UUID" }
  ]
},
{
  "tag": "command",
  "package": "content.workflow",
  "name": "ContinuePublicationAfterMediaSucceeded",
  "desc": "continue publication after media processing succeeded",
  "aggregates": ["Content"],
  "requestFields": [
    { "name": "contentId", "type": "java.util.UUID" },
    { "name": "mediaProcessingTaskId", "type": "java.util.UUID" }
  ],
  "responseFields": []
},
{
  "tag": "command",
  "package": "release.readiness",
  "name": "OpenPublicationReleaseReadiness",
  "desc": "open publication release readiness for gated content",
  "aggregates": ["PublicationReleaseReadiness"],
  "requestFields": [
    { "name": "contentId", "type": "java.util.UUID" },
    { "name": "mediaProcessingTaskId", "type": "java.util.UUID" },
    { "name": "releaseWindowOpensAt", "type": "java.time.LocalDateTime" },
    { "name": "releaseWindowClosesAt", "type": "java.time.LocalDateTime" }
  ],
  "responseFields": [
    { "name": "readinessId", "type": "java.util.UUID" }
  ]
},
{
  "tag": "command",
  "package": "release.readiness",
  "name": "PassCopyrightReview",
  "desc": "pass copyright review for gated publication",
  "aggregates": ["PublicationReleaseReadiness"],
  "requestFields": [
    { "name": "contentId", "type": "java.util.UUID" }
  ],
  "responseFields": []
},
{
  "tag": "command",
  "package": "release.readiness",
  "name": "ConfirmManualRelease",
  "desc": "confirm manual release for gated publication",
  "aggregates": ["PublicationReleaseReadiness"],
  "requestFields": [
    { "name": "contentId", "type": "java.util.UUID" }
  ],
  "responseFields": []
},
{
  "tag": "command",
  "package": "release.readiness",
  "name": "CompletePublicationReleaseReadiness",
  "desc": "complete release readiness and publish content",
  "aggregates": ["PublicationReleaseReadiness"],
  "requestFields": [
    { "name": "contentId", "type": "java.util.UUID" },
    { "name": "completedAt", "type": "java.time.LocalDateTime" }
  ],
  "responseFields": []
},
{
  "tag": "api_payload",
  "package": "content.workflow",
  "name": "CreateGatedContentDraftPayload",
  "desc": "create gated content draft http payload",
  "aggregates": [],
  "requestFields": [
    { "name": "title", "type": "String" },
    { "name": "body", "type": "String" },
    { "name": "mediaSourceKey", "type": "String" },
    { "name": "releaseWindowOpensAt", "type": "java.time.LocalDateTime" },
    { "name": "releaseWindowClosesAt", "type": "java.time.LocalDateTime" }
  ],
  "responseFields": [
    { "name": "contentId", "type": "java.util.UUID" }
  ]
}
```

- [ ] **Step 4: Generate and sync snapshots**

Run:

```powershell
.\gradlew.bat cap4kPlan cap4kGenerate syncGeneratedSnapshots
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Inspect generated snapshot shape**

Run:

```powershell
rg -n "ReleasePolicy|PublicationReleaseReadiness|CreateGatedContentDraft|OpenPublicationReleaseReadiness" `
  cap4k-reference-content-studio-domain/src-generated `
  cap4k-reference-content-studio-application/src/main `
  cap4k-reference-content-studio-adapter/src/main
```

Expected: matches for `ReleasePolicy`, generated readiness aggregate files, and generated advanced command/payload skeletons.

- [ ] **Step 6: Commit schema and generated skeletons**

Run:

```powershell
git add build.gradle.kts design/design.json `
  cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql `
  cap4k-reference-content-studio-domain/src-generated `
  cap4k-reference-content-studio-application/src/main `
  cap4k-reference-content-studio-adapter/src/main
git commit -m "feat: add advanced publication generation inputs"
```

---

### Task 2: Separate-Table Media Result Value Object

**Files:**
- Create: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/values/MediaProcessingResultStatus.kt`
- Create: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/values/MediaProcessingResultSnapshot.kt`
- Create: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/values/MediaProcessingResultSnapshotTest.kt`

- [ ] **Step 1: Write value-object hash test**

Create `MediaProcessingResultSnapshotTest.kt`:

```kotlin
package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values

import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class MediaProcessingResultSnapshotTest {
    private val taskId = UUID.fromString("00000000-0000-0000-0000-000000000101")
    private val completedAt = LocalDateTime.parse("2026-05-11T10:15:30")

    @Test
    fun `hash is stable for the same business value`() {
        val first = snapshot(assetSha256 = "a".repeat(64))
        val second = snapshot(assetSha256 = "a".repeat(64))

        assertEquals(first.hash(), second.hash())
        assertEquals(first.id, second.id)
    }

    @Test
    fun `hash changes when result business value changes`() {
        val first = snapshot(assetSha256 = "a".repeat(64))
        val second = snapshot(assetSha256 = "b".repeat(64))

        assertNotEquals(first.hash(), second.hash())
    }

    @Test
    fun `blank external task id is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            snapshot(externalTaskId = " ")
        }
    }

    private fun snapshot(
        externalTaskId: String = "external-123",
        assetSha256: String = "a".repeat(64),
    ) = MediaProcessingResultSnapshot.create(
        mediaProcessingTaskId = taskId,
        externalTaskId = externalTaskId,
        resultStatus = MediaProcessingResultStatus.SUCCEEDED,
        assetSha256 = assetSha256,
        assetLocation = "s3://content-studio/assets/external-123.mp4",
        completedAt = completedAt,
        now = completedAt,
    )
}
```

- [ ] **Step 2: Run test and verify it fails**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*MediaProcessingResultSnapshotTest"
```

Expected: FAIL because `MediaProcessingResultSnapshot` does not exist.

- [ ] **Step 3: Implement result status enum**

Create `MediaProcessingResultStatus.kt`:

```kotlin
package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values

import jakarta.persistence.AttributeConverter

enum class MediaProcessingResultStatus(
    val value: Int,
    val description: String,
) {
    SUCCEEDED(0, "Succeeded"),
    FAILED(1, "Failed");

    companion object {
        private val enumMap = entries.associateBy { it.value }

        fun valueOfOrNull(value: Int?): MediaProcessingResultStatus? = enumMap[value]
    }

    @jakarta.persistence.Converter(autoApply = false)
    class Converter : AttributeConverter<MediaProcessingResultStatus, Int> {
        override fun convertToDatabaseColumn(attribute: MediaProcessingResultStatus?): Int? = attribute?.value

        override fun convertToEntityAttribute(dbData: Int?): MediaProcessingResultStatus? = valueOfOrNull(dbData)
    }
}
```

- [ ] **Step 4: Implement separate-table value object**

Create `MediaProcessingResultSnapshot.kt`:

```kotlin
package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values

import com.only4.cap4k.ddd.core.domain.aggregate.ValueObject
import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "media_processing_result_snapshot")
@Aggregate(
    aggregate = "MediaProcessingTask",
    name = "MediaProcessingResultSnapshot",
    type = Aggregate.TYPE_VALUE_OBJECT,
    description = "Media processing result snapshot"
)
class MediaProcessingResultSnapshot(
    mediaProcessingTaskId: UUID,
    externalTaskId: String,
    resultStatus: MediaProcessingResultStatus,
    assetSha256: String,
    assetLocation: String,
    completedAt: LocalDateTime,
    dbCreatedAt: LocalDateTime,
    dbUpdatedAt: LocalDateTime,
    id: String = computeHash(
        mediaProcessingTaskId = mediaProcessingTaskId,
        externalTaskId = externalTaskId,
        resultStatus = resultStatus,
        assetSha256 = assetSha256,
        assetLocation = assetLocation,
    ),
) : ValueObject<String> {

    @Id
    @Column(name = "id")
    var id: String = id
        internal set

    @Column(name = "media_processing_task_id")
    var mediaProcessingTaskId: UUID = mediaProcessingTaskId
        internal set

    @Column(name = "external_task_id")
    var externalTaskId: String = externalTaskId
        internal set

    @Column(name = "result_status")
    @Convert(converter = MediaProcessingResultStatus.Converter::class)
    var resultStatus: MediaProcessingResultStatus = resultStatus
        internal set

    @Column(name = "asset_sha256")
    var assetSha256: String = assetSha256
        internal set

    @Column(name = "asset_location")
    var assetLocation: String = assetLocation
        internal set

    @Column(name = "completed_at")
    var completedAt: LocalDateTime = completedAt
        internal set

    @Column(name = "db_created_at")
    var dbCreatedAt: LocalDateTime = dbCreatedAt
        internal set

    @Column(name = "db_updated_at")
    var dbUpdatedAt: LocalDateTime = dbUpdatedAt
        internal set

    override fun hash(): String = id

    companion object {
        fun create(
            mediaProcessingTaskId: UUID,
            externalTaskId: String,
            resultStatus: MediaProcessingResultStatus,
            assetSha256: String,
            assetLocation: String,
            completedAt: LocalDateTime,
            now: LocalDateTime,
        ): MediaProcessingResultSnapshot {
            require(externalTaskId.isNotBlank()) { "External task id must not be blank." }
            require(assetSha256.matches(Regex("[a-fA-F0-9]{64}"))) {
                "Asset SHA-256 must be a 64-character hexadecimal value."
            }
            require(assetLocation.isNotBlank()) { "Asset location must not be blank." }
            return MediaProcessingResultSnapshot(
                mediaProcessingTaskId = mediaProcessingTaskId,
                externalTaskId = externalTaskId.trim(),
                resultStatus = resultStatus,
                assetSha256 = assetSha256.lowercase(),
                assetLocation = assetLocation.trim(),
                completedAt = completedAt,
                dbCreatedAt = now,
                dbUpdatedAt = now,
            )
        }

        fun computeHash(
            mediaProcessingTaskId: UUID,
            externalTaskId: String,
            resultStatus: MediaProcessingResultStatus,
            assetSha256: String,
            assetLocation: String,
        ): String {
            val raw = listOf(
                mediaProcessingTaskId.toString(),
                externalTaskId.trim(),
                resultStatus.name,
                assetSha256.lowercase(),
                assetLocation.trim(),
            ).joinToString("|")
            val bytes = MessageDigest.getInstance("MD5").digest(raw.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
```

- [ ] **Step 5: Run value-object tests**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*MediaProcessingResultSnapshotTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit value object**

Run:

```powershell
git add cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/values `
  cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/values
git commit -m "feat: add media processing result value object"
```

---

### Task 3: Media Processing Result Flow

**Files:**
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/media/processing/MarkMediaProcessingSucceededCmd.kt`
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/MediaProcessingTaskBehavior.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/integration/MediaProcessingCallbackIntegrationEvent.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/integration/MediaProcessingCallbackIntegrationEventSubscriber.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/jobs/MediaProcessingPollingFallbackJob.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/distributed/clients/media/processing/GetMediaProcessingStatusCli.kt`
- Modify: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/integration/FakeMediaProcessingCli.kt`
- Modify: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/application/distributed/clients/media/processing/GetMediaProcessingStatusCliHandler.kt`
- Modify: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/MediaProcessingTaskBehaviorTest.kt`
- Modify: `http/media-processing.http`

- [ ] **Step 1: Update media processing behavior test**

In `MediaProcessingTaskBehaviorTest`, update the successful mark test so it creates a snapshot and asserts the existing domain event still fires:

```kotlin
val snapshot = MediaProcessingResultSnapshot.create(
    mediaProcessingTaskId = task.id,
    externalTaskId = "external-123",
    resultStatus = MediaProcessingResultStatus.SUCCEEDED,
    assetSha256 = "a".repeat(64),
    assetLocation = "s3://content-studio/assets/external-123.mp4",
    completedAt = LocalDateTime.parse("2026-05-11T10:15:30"),
    now = LocalDateTime.parse("2026-05-11T10:15:30"),
)

task.markSucceeded(snapshot)
```

Expected assertion remains:

```kotlin
assertEquals(MediaProcessingStatus.SUCCEEDED, task.processingStatus)
assertEquals(task.id, event.taskId)
assertEquals(task.contentId, event.contentId)
assertEquals("external-123", event.externalTaskId)
```

- [ ] **Step 2: Run behavior test and verify it fails**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*MediaProcessingTaskBehaviorTest"
```

Expected: FAIL because `markSucceeded(snapshot)` does not exist.

- [ ] **Step 3: Change behavior signature**

Modify `MediaProcessingTaskBehavior.kt`:

```kotlin
fun MediaProcessingTask.markSucceeded(resultSnapshot: MediaProcessingResultSnapshot) {
    if (processingStatus == MediaProcessingStatus.SUCCEEDED) {
        return
    }

    check(processingStatus == MediaProcessingStatus.SUBMITTED) {
        "Cannot mark a media processing task as succeeded before it has been submitted."
    }

    check(!externalTaskId.isNullOrBlank()) {
        "Cannot mark a media processing task as succeeded without an external task id."
    }

    check(resultSnapshot.mediaProcessingTaskId == id) {
        "Media processing result snapshot does not belong to this task."
    }

    check(resultSnapshot.externalTaskId == externalTaskId) {
        "Media processing result snapshot external task id does not match this task."
    }

    processingStatus = MediaProcessingStatus.SUCCEEDED
    events().attach(this) {
        MediaProcessingSucceededDomainEvent(
            entity = this,
            taskId = id,
            contentId = contentId,
            externalTaskId = externalTaskId,
        )
    }
}
```

Add imports:

```kotlin
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values.MediaProcessingResultSnapshot
```

- [ ] **Step 4: Update command request and persist value object**

Modify `MarkMediaProcessingSucceededCmd.Request`:

```kotlin
data class Request(
    val externalTaskId: String,
    val assetSha256: String,
    val assetLocation: String,
    val completedAt: java.time.LocalDateTime,
) : RequestParam<Response>
```

Inside `Handler.exec`, after loading `task`, create and persist the value object:

```kotlin
val now = java.time.LocalDateTime.now()
val resultSnapshot = MediaProcessingResultSnapshot.create(
    mediaProcessingTaskId = task.id,
    externalTaskId = request.externalTaskId,
    resultStatus = MediaProcessingResultStatus.SUCCEEDED,
    assetSha256 = request.assetSha256,
    assetLocation = request.assetLocation,
    completedAt = request.completedAt,
    now = now,
)
task.markSucceeded(resultSnapshot)
Mediator.uow.persist(resultSnapshot)
Mediator.uow.save()
```

Add imports:

```kotlin
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values.MediaProcessingResultSnapshot
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values.MediaProcessingResultStatus
```

- [ ] **Step 5: Update callback event and subscriber**

Modify `MediaProcessingCallbackIntegrationEvent`:

```kotlin
data class MediaProcessingCallbackIntegrationEvent(
    val externalTaskId: String,
    val status: String,
    val assetSha256: String,
    val assetLocation: String,
    val completedAt: java.time.LocalDateTime,
)
```

Modify subscriber command send:

```kotlin
Mediator.cmd.send(
    MarkMediaProcessingSucceededCmd.Request(
        externalTaskId = event.externalTaskId,
        assetSha256 = event.assetSha256,
        assetLocation = event.assetLocation,
        completedAt = event.completedAt,
    )
)
```

- [ ] **Step 6: Update polling fallback and fake external status**

Modify `GetMediaProcessingStatusCli.Response`:

```kotlin
data class Response(
    val status: String,
    val assetSha256: String?,
    val assetLocation: String?,
    val completedAt: java.time.LocalDateTime?,
)
```

Modify polling success branch:

```kotlin
if (status.status == "SUCCEEDED") {
    Mediator.cmd.send(
        MarkMediaProcessingSucceededCmd.Request(
            externalTaskId = externalTaskId,
            assetSha256 = requireNotNull(status.assetSha256),
            assetLocation = requireNotNull(status.assetLocation),
            completedAt = requireNotNull(status.completedAt),
        )
    )
}
```

Modify fake CLI to return deterministic values:

```kotlin
GetMediaProcessingStatusCli.Response(
    status = "SUCCEEDED",
    assetSha256 = "a".repeat(64),
    assetLocation = "s3://content-studio/assets/$externalTaskId.mp4",
    completedAt = java.time.LocalDateTime.parse("2026-05-11T10:15:30"),
)
```

- [ ] **Step 7: Update callback `.http` payload**

Modify `http/media-processing.http` body:

```json
{
  "externalTaskId": "{{externalTaskId}}",
  "status": "SUCCEEDED",
  "assetSha256": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
  "assetLocation": "s3://content-studio/assets/{{externalTaskId}}.mp4",
  "completedAt": "2026-05-11T10:15:30"
}
```

- [ ] **Step 8: Run targeted tests**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*MediaProcessingTaskBehaviorTest"
.\gradlew.bat :cap4k-reference-content-studio-adapter:test --tests "*MediaProcessingCallbackContractTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit media result flow**

Run:

```powershell
git add cap4k-reference-content-studio-domain/src/main/kotlin `
  cap4k-reference-content-studio-domain/src/test/kotlin `
  cap4k-reference-content-studio-application/src/main/kotlin `
  cap4k-reference-content-studio-adapter/src/main/kotlin `
  cap4k-reference-content-studio-adapter/src/test/kotlin `
  http/media-processing.http
git commit -m "feat: capture media processing result snapshots"
```

---

### Task 4: Release Policy and Publication Eligibility

**Files:**
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/services/PublicationEligibilityDomainService.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/CreateContentDraftCmd.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/CreateGatedContentDraftCmd.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/PublishContentCmd.kt`
- Modify: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/services/PublicationEligibilityDomainServiceTest.kt`

- [ ] **Step 1: Update domain service tests for structured conclusions**

Replace boolean assertions in `PublicationEligibilityDomainServiceTest` with:

```kotlin
assertEquals(PublicationEligibilityDecision.Eligible, service.evaluate(content, task))
```

Add tests for:

```kotlin
assertEquals(PublicationEligibilityDecision.ContentNotApproved, service.evaluate(unapprovedContent, succeededTask))
assertEquals(PublicationEligibilityDecision.MediaProcessingNotSucceeded, service.evaluate(approvedContent, submittedTask))
assertEquals(PublicationEligibilityDecision.TaskDoesNotBelongToContent, service.evaluate(approvedContent, otherContentTask))
```

- [ ] **Step 2: Run service test and verify it fails**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*PublicationEligibilityDomainServiceTest"
```

Expected: FAIL because `PublicationEligibilityDecision` does not exist.

- [ ] **Step 3: Implement structured domain conclusion**

Modify `PublicationEligibilityDomainService.kt`:

```kotlin
package com.only4.cap4k.reference.contentstudio.domain.services

import com.only4.cap4k.ddd.core.domain.service.annotation.DomainService
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import org.springframework.stereotype.Service

sealed interface PublicationEligibilityDecision {
    data object Eligible : PublicationEligibilityDecision
    data object ContentNotApproved : PublicationEligibilityDecision
    data object MediaProcessingNotSucceeded : PublicationEligibilityDecision
    data object TaskDoesNotBelongToContent : PublicationEligibilityDecision
    data object ReleaseReadinessNotSatisfied : PublicationEligibilityDecision
}

@DomainService
@Service
class PublicationEligibilityDomainService {
    fun evaluate(
        content: Content,
        task: MediaProcessingTask,
        releaseReadinessSatisfied: Boolean = true,
    ): PublicationEligibilityDecision {
        if (task.contentId != content.id) return PublicationEligibilityDecision.TaskDoesNotBelongToContent
        if (content.reviewStatus != ReviewStatus.APPROVED) return PublicationEligibilityDecision.ContentNotApproved
        if (task.processingStatus != MediaProcessingStatus.SUCCEEDED) {
            return PublicationEligibilityDecision.MediaProcessingNotSucceeded
        }
        if (!releaseReadinessSatisfied) return PublicationEligibilityDecision.ReleaseReadinessNotSatisfied
        return PublicationEligibilityDecision.Eligible
    }
}
```

- [ ] **Step 4: Update create commands for release policy**

In `CreateContentDraftCmd`, set generated `ContentFactory.Payload` fields:

```kotlin
releasePolicy = ReleasePolicy.IMMEDIATE,
releaseWindowOpensAt = null,
releaseWindowClosesAt = null,
```

In `CreateGatedContentDraftCmd`, implement handler body using `Mediator.factories.create(ContentFactory.Payload(...))` with:

```kotlin
releasePolicy = ReleasePolicy.GATED,
releaseWindowOpensAt = request.releaseWindowOpensAt,
releaseWindowClosesAt = request.releaseWindowClosesAt,
```

Before creating content, validate:

```kotlin
check(request.releaseWindowClosesAt.isAfter(request.releaseWindowOpensAt)) {
    "Release window close time must be after open time."
}
```

- [ ] **Step 5: Update publish command to interpret decision**

In `PublishContentCmd`, replace boolean check with:

```kotlin
val decision = publicationEligibilityDomainService.evaluate(content, mediaProcessingTask)
check(decision == PublicationEligibilityDecision.Eligible) {
    "Content ${request.contentId} is not eligible for publication: $decision."
}
```

Add import:

```kotlin
import com.only4.cap4k.reference.contentstudio.domain.services.PublicationEligibilityDecision
```

- [ ] **Step 6: Run targeted tests**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*PublicationEligibilityDomainServiceTest"
.\gradlew.bat :cap4k-reference-content-studio-application:compileKotlin
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit release policy and eligibility**

Run:

```powershell
git add cap4k-reference-content-studio-domain/src/main/kotlin `
  cap4k-reference-content-studio-domain/src/test/kotlin `
  cap4k-reference-content-studio-application/src/main/kotlin
git commit -m "feat: add release policy and eligibility decisions"
```

---

### Task 5: Publication Release Readiness Process

**Files:**
- Create: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/publication_release_readiness/PublicationReleaseReadinessBehavior.kt`
- Create: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/publication_release_readiness/PublicationReleaseReadinessBehaviorTest.kt`
- Modify/create commands under `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/release/readiness/`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/ContinuePublicationAfterMediaSucceededCmd.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/domain/media_processing_task/MediaProcessingSucceededDomainEventSubscriber.kt`

- [ ] **Step 1: Write readiness behavior tests**

Create `PublicationReleaseReadinessBehaviorTest.kt` with tests:

```kotlin
package com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness

import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.CopyrightReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.ManualReleaseConfirmationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.PublicationReleaseReadinessState
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PublicationReleaseReadinessBehaviorTest {
    @Test
    fun `waiting readiness becomes ready after copyright manual confirmation and open window`() {
        val readiness = readiness()
        val now = LocalDateTime.parse("2026-05-11T12:00:00")

        readiness.passCopyrightReview()
        readiness.confirmManualRelease()
        readiness.complete(now)

        assertEquals(PublicationReleaseReadinessState.READY, readiness.readinessState)
        assertEquals(now, readiness.readyAt)
    }

    @Test
    fun `readiness cannot complete before release window opens`() {
        val readiness = readiness()
        readiness.passCopyrightReview()
        readiness.confirmManualRelease()

        assertThrows(IllegalStateException::class.java) {
            readiness.complete(LocalDateTime.parse("2026-05-11T09:59:59"))
        }
    }

    private fun readiness() = PublicationReleaseReadiness(
        id = UUID.fromString("00000000-0000-0000-0000-000000000301"),
        contentId = UUID.fromString("00000000-0000-0000-0000-000000000302"),
        mediaProcessingTaskId = UUID.fromString("00000000-0000-0000-0000-000000000303"),
        readinessState = PublicationReleaseReadinessState.WAITING,
        copyrightStatus = CopyrightReviewStatus.WAITING,
        manualConfirmationStatus = ManualReleaseConfirmationStatus.WAITING,
        releaseWindowOpensAt = LocalDateTime.parse("2026-05-11T10:00:00"),
        releaseWindowClosesAt = LocalDateTime.parse("2026-05-12T10:00:00"),
        readyAt = null,
        cancelReason = null,
        dbCreatedAt = LocalDateTime.parse("2026-05-11T09:00:00"),
        dbUpdatedAt = LocalDateTime.parse("2026-05-11T09:00:00"),
    )
}
```

- [ ] **Step 2: Run readiness behavior tests and verify failure**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*PublicationReleaseReadinessBehaviorTest"
```

Expected: FAIL because behavior functions do not exist.

- [ ] **Step 3: Implement readiness behavior**

Create `PublicationReleaseReadinessBehavior.kt`:

```kotlin
package com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness

import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.CopyrightReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.ManualReleaseConfirmationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.PublicationReleaseReadinessState
import java.time.LocalDateTime

fun PublicationReleaseReadiness.passCopyrightReview() {
    check(readinessState == PublicationReleaseReadinessState.WAITING) {
        "Cannot pass copyright review when release readiness is not waiting."
    }
    copyrightStatus = CopyrightReviewStatus.PASSED
}

fun PublicationReleaseReadiness.confirmManualRelease() {
    check(readinessState == PublicationReleaseReadinessState.WAITING) {
        "Cannot confirm manual release when release readiness is not waiting."
    }
    manualConfirmationStatus = ManualReleaseConfirmationStatus.CONFIRMED
}

fun PublicationReleaseReadiness.complete(now: LocalDateTime) {
    check(readinessState == PublicationReleaseReadinessState.WAITING) {
        "Cannot complete release readiness when it is not waiting."
    }
    check(copyrightStatus == CopyrightReviewStatus.PASSED) {
        "Cannot complete release readiness before copyright review passes."
    }
    check(manualConfirmationStatus == ManualReleaseConfirmationStatus.CONFIRMED) {
        "Cannot complete release readiness before manual release is confirmed."
    }
    check(!now.isBefore(releaseWindowOpensAt)) {
        "Cannot complete release readiness before release window opens."
    }
    check(now.isBefore(releaseWindowClosesAt) || now == releaseWindowClosesAt) {
        "Cannot complete release readiness after release window closes."
    }

    readinessState = PublicationReleaseReadinessState.READY
    readyAt = now
    dbUpdatedAt = now
}
```

- [ ] **Step 4: Implement readiness commands**

For each command in `application/commands/release/readiness`, use `Mediator.repositories`, `Mediator.factories`, and `Mediator.uow`.

`OpenPublicationReleaseReadinessCmd.Handler.exec` should create if absent:

```kotlin
val existing = Mediator.repositories.findFirst(
    SPublicationReleaseReadiness.predicate { schema ->
        schema.contentId.eq(request.contentId)
    }
)
if (existing != null) {
    return Response(readinessId = existing.id)
}
val now = LocalDateTime.now()
val readiness = Mediator.factories.create(
    PublicationReleaseReadinessFactory.Payload(
        id = UUID.randomUUID(),
        contentId = request.contentId,
        mediaProcessingTaskId = request.mediaProcessingTaskId,
        readinessState = PublicationReleaseReadinessState.WAITING,
        copyrightStatus = CopyrightReviewStatus.WAITING,
        manualConfirmationStatus = ManualReleaseConfirmationStatus.WAITING,
        releaseWindowOpensAt = request.releaseWindowOpensAt,
        releaseWindowClosesAt = request.releaseWindowClosesAt,
        readyAt = null,
        cancelReason = null,
        dbCreatedAt = now,
        dbUpdatedAt = now,
    )
)
Mediator.uow.save()
return Response(readinessId = readiness.id)
```

`PassCopyrightReviewCmd` and `ConfirmManualReleaseCmd` should load by `contentId`, call behavior, then `Mediator.uow.save()`.

`CompletePublicationReleaseReadinessCmd` should load readiness, call `complete(request.completedAt)`, save it, then send:

```kotlin
Mediator.cmd.send(
    PublishContentCmd.Request(
        contentId = request.contentId,
        publishedAt = request.completedAt,
    )
)
```

- [ ] **Step 5: Implement media-succeeded continuation split**

Modify subscriber to only send continuation command:

```kotlin
@EventListener(MediaProcessingSucceededDomainEvent::class)
fun onMediaProcessingSucceeded(event: MediaProcessingSucceededDomainEvent) {
    Mediator.cmd.send(
        ContinuePublicationAfterMediaSucceededCmd.Request(
            contentId = event.contentId,
            mediaProcessingTaskId = event.taskId,
        )
    )
}
```

Implement `ContinuePublicationAfterMediaSucceededCmd.Handler.exec`:

```kotlin
val content = checkNotNull(Mediator.repositories.findOne(SContent.predicateById(request.contentId))) {
    "Content ${request.contentId} was not found."
}
when (content.releasePolicy) {
    ReleasePolicy.IMMEDIATE -> Mediator.cmd.send(
        PublishContentCmd.Request(
            contentId = request.contentId,
            publishedAt = LocalDateTime.now(),
        )
    )
    ReleasePolicy.GATED -> Mediator.cmd.send(
        OpenPublicationReleaseReadinessCmd.Request(
            contentId = request.contentId,
            mediaProcessingTaskId = request.mediaProcessingTaskId,
            releaseWindowOpensAt = requireNotNull(content.releaseWindowOpensAt),
            releaseWindowClosesAt = requireNotNull(content.releaseWindowClosesAt),
        )
    )
}
return Response
```

- [ ] **Step 6: Run targeted compile and tests**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*PublicationReleaseReadinessBehaviorTest"
.\gradlew.bat :cap4k-reference-content-studio-application:compileKotlin
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit readiness process**

Run:

```powershell
git add cap4k-reference-content-studio-domain/src/main/kotlin `
  cap4k-reference-content-studio-domain/src/test/kotlin `
  cap4k-reference-content-studio-application/src/main/kotlin
git commit -m "feat: add gated publication readiness process"
```

---

### Task 6: HTTP Advanced Path

**Files:**
- Create: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/http/AdvancedReleaseReadinessController.kt`
- Modify/create payload files under `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/portal/api/payload/content/workflow/`
- Create: `http/advanced-release-readiness.http`
- Modify: `cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/ContentStudioAdvancedReleaseReadinessHttpSmokeTest.kt`

- [ ] **Step 1: Create advanced controller**

Create `AdvancedReleaseReadinessController.kt`:

```kotlin
package com.only4.cap4k.reference.contentstudio.adapter.http

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.workflow.CreateGatedContentDraftPayload
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.CreateGatedContentDraftCmd
import com.only4.cap4k.reference.contentstudio.application.commands.release.readiness.CompletePublicationReleaseReadinessCmd
import com.only4.cap4k.reference.contentstudio.application.commands.release.readiness.ConfirmManualReleaseCmd
import com.only4.cap4k.reference.contentstudio.application.commands.release.readiness.PassCopyrightReviewCmd
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/advanced/contents")
class AdvancedReleaseReadinessController {
    @PostMapping("/gated")
    fun createGated(@RequestBody request: CreateGatedContentDraftPayload.Request): CreateGatedContentDraftPayload.Response {
        val response = Mediator.cmd.send(
            CreateGatedContentDraftCmd.Request(
                title = request.title,
                body = request.body,
                mediaSourceKey = request.mediaSourceKey,
                releaseWindowOpensAt = request.releaseWindowOpensAt,
                releaseWindowClosesAt = request.releaseWindowClosesAt,
            )
        )
        return CreateGatedContentDraftPayload.Response(contentId = response.contentId)
    }

    @PostMapping("/{contentId}/release-readiness/copyright-pass")
    fun passCopyright(@PathVariable contentId: UUID) {
        Mediator.cmd.send(PassCopyrightReviewCmd.Request(contentId = contentId))
    }

    @PostMapping("/{contentId}/release-readiness/manual-confirm")
    fun confirmManual(@PathVariable contentId: UUID) {
        Mediator.cmd.send(ConfirmManualReleaseCmd.Request(contentId = contentId))
    }

    @PostMapping("/{contentId}/release-readiness/complete")
    fun complete(@PathVariable contentId: UUID) {
        Mediator.cmd.send(
            CompletePublicationReleaseReadinessCmd.Request(
                contentId = contentId,
                completedAt = LocalDateTime.now(),
            )
        )
    }
}
```

- [ ] **Step 2: Create advanced `.http` path**

Create `http/advanced-release-readiness.http`:

```http
@baseUrl = http://localhost:8080
@contentId =
@externalTaskId =

### Create gated content draft.
POST {{baseUrl}}/advanced/contents/gated
Content-Type: application/json
Accept: application/json

{
  "title": "Gated sample content",
  "body": "Sample gated content body",
  "mediaSourceKey": "sample-gated-media-source-key",
  "releaseWindowOpensAt": "2026-05-11T00:00:00",
  "releaseWindowClosesAt": "2026-05-12T00:00:00"
}

### Then run review.http submit and approve with the created contentId.
### Then run query.http to copy task.externalTaskId.
### Then run media-processing.http with the copied externalTaskId.

### Pass copyright review.
POST {{baseUrl}}/advanced/contents/{{contentId}}/release-readiness/copyright-pass
Accept: application/json

### Confirm manual release.
POST {{baseUrl}}/advanced/contents/{{contentId}}/release-readiness/manual-confirm
Accept: application/json

### Complete release readiness and publish.
POST {{baseUrl}}/advanced/contents/{{contentId}}/release-readiness/complete
Accept: application/json
```

- [ ] **Step 3: Add advanced HTTP smoke test**

Create `ContentStudioAdvancedReleaseReadinessHttpSmokeTest.kt` by following the existing `ContentStudioHappyPathHttpSmokeTest` style. The test must:

```kotlin
@Test
fun `advanced gated path waits for release readiness before publishing`() {
    // create gated content
    // submit review
    // approve review
    // query media task
    // send callback
    // assert content is not PUBLISHED yet
    // pass copyright
    // confirm manual release
    // complete readiness
    // assert content is PUBLISHED
}
```

Keep this as a teaching smoke test. Do not assert internal table rows through repositories from the test.

- [ ] **Step 4: Run smoke tests**

Run:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-start:test --tests "*ContentStudioHappyPathHttpSmokeTest"
.\gradlew.bat :cap4k-reference-content-studio-start:test --tests "*ContentStudioAdvancedReleaseReadinessHttpSmokeTest"
```

Expected: both commands `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit HTTP advanced path**

Run:

```powershell
git add cap4k-reference-content-studio-adapter/src/main/kotlin `
  cap4k-reference-content-studio-start/src/test/kotlin `
  http/advanced-release-readiness.http
git commit -m "feat: expose gated release readiness path"
```

---

### Task 7: README and Analysis Refresh

**Files:**
- Modify: `README.zh-CN.md`
- Modify: `README.md`
- Modify: `analysis/flows/**`
- Modify: `openapi/content-studio-openapi.json` if runtime OpenAPI snapshot is refreshed by existing project convention

- [ ] **Step 1: Update README advanced mapping**

Add a short advanced examples section after the current happy-path explanation:

```markdown
## Advanced authoring examples

The default path still uses immediate publication: after media processing succeeds,
the application publishes the content through `PublishContentCmd`.

The advanced path is opt-in:

- `MediaProcessingResultSnapshot` demonstrates separate-table value-object persistence.
- `PublicationEligibilityDomainService` returns an auditable publication decision.
- Gated content uses `PublicationReleaseReadiness` to record cross-time waiting state before publishing.

The gated path is a Saga/process candidate, but this reference project keeps it as an explicit
process-state aggregate instead of enabling Saga runtime by default.
```

Add the same meaning to `README.zh-CN.md` in Chinese:

```markdown
## 高级编写示例映射

默认路径仍然是即时发布：媒体处理成功后，application 通过 `PublishContentCmd`
发布内容。

高级路径是显式 opt-in：

- `MediaProcessingResultSnapshot` 演示 separate-table value object 持久化。
- `PublicationEligibilityDomainService` 返回可审计的发布资格结论。
- gated content 使用 `PublicationReleaseReadiness` 记录跨时间等待状态，再进入发布。

这条 gated 路线是 Saga / process 候选边界，但本参考项目默认不启用 Saga runtime，
而是先用明确的流程状态聚合表达等待点。
```

- [ ] **Step 2: Refresh analysis**

Run:

```powershell
.\gradlew.bat cap4kAnalysisPlan
.\gradlew.bat cap4kAnalysisGenerate
```

Expected: both commands `BUILD SUCCESSFUL`.

- [ ] **Step 3: Verify analysis mentions both immediate and advanced paths**

Run:

```powershell
rg -n "ContinuePublicationAfterMediaSucceeded|PublicationReleaseReadiness|CompletePublicationReleaseReadiness|PublishContentCmd" analysis/flows
```

Expected: matches in generated JSON/Mermaid flow files.

- [ ] **Step 4: Commit docs and analysis**

Run:

```powershell
git add README.md README.zh-CN.md analysis/flows openapi/content-studio-openapi.json
git commit -m "docs: map advanced examples to reference project"
```

If `openapi/content-studio-openapi.json` did not change, omit it from `git add`.

---

### Task 8: Full Verification and Teaching Cleanup

**Files:**
- Delete: `docs/`
- Review: all `src/test/**`
- Review: `README.md`, `README.zh-CN.md`, `http/*.http`

- [ ] **Step 1: Run full verification**

Run:

```powershell
.\gradlew.bat clean test
.\gradlew.bat cap4kPlan cap4kGenerate syncGeneratedSnapshots
.\gradlew.bat cap4kAnalysisPlan cap4kAnalysisGenerate
```

Expected: all commands `BUILD SUCCESSFUL`.

- [ ] **Step 2: Scan tests for crime-scene names**

Run:

```powershell
rg -n "Residue|Regression|Bug|Temporary|Workaround|ArchitectureContract|TacticalContract|案发|临时|残留" `
  cap4k-reference-content-studio-domain/src/test `
  cap4k-reference-content-studio-application/src/test `
  cap4k-reference-content-studio-adapter/src/test `
  cap4k-reference-content-studio-start/src/test
```

Expected: no matches. If a match is a useful teaching test, rename it to describe business behavior.

- [ ] **Step 3: Keep only teaching tests**

Allowed final test categories:

```text
domain behavior tests
factory tests
domain service tests
callback contract test
default HTTP happy-path smoke test
advanced gated-path smoke test
minimal application boot smoke test
```

Delete tests that only document a development failure and are not useful to a reader learning the reference project.

- [ ] **Step 4: Delete working docs**

Delete the implementation-only docs directory:

```powershell
Remove-Item -LiteralPath docs -Recurse -Force
```

Then verify:

```powershell
Test-Path docs
```

Expected output:

```text
False
```

- [ ] **Step 5: Verify no public README points to deleted docs**

Run:

```powershell
rg -n "docs/superpowers|advanced-authoring-examples-implementation|docs/" README.md README.zh-CN.md
```

Expected: no references to `docs/superpowers` or the removed implementation docs.

- [ ] **Step 6: Run final status and diff checks**

Run:

```powershell
git diff --check
git status --short --branch
```

Expected:

```text
git diff --check exits 0
```

`git status --short --branch` may show staged/uncommitted deletion of `docs/` before the final commit.

- [ ] **Step 7: Commit cleanup**

Run:

```powershell
git add -A
git commit -m "chore: clean reference project delivery artifacts"
```

- [ ] **Step 8: Verify final branch surface**

Run:

```powershell
git status --short --branch
Test-Path docs
rg -n "Residue|Regression|Bug|Temporary|Workaround|ArchitectureContract|TacticalContract|案发|临时|残留" `
  cap4k-reference-content-studio-domain/src/test `
  cap4k-reference-content-studio-application/src/test `
  cap4k-reference-content-studio-adapter/src/test `
  cap4k-reference-content-studio-start/src/test
```

Expected:

```text
working tree clean
Test-Path docs -> False
test scan has no crime-scene matches
```

Only after this step should the branch be considered ready for master review.
