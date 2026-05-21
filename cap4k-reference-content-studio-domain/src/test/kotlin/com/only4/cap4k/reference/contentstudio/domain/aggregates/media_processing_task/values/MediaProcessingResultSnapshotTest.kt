package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values

import com.only4.cap4k.ddd.core.domain.aggregate.ValueObject
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class MediaProcessingResultSnapshotTest {
    private val taskId = UUID.fromString("00000000-0000-0000-0000-000000000101")
    private val contentId = UUID.fromString("00000000-0000-0000-0000-000000000202")
    private val completedAt = LocalDateTime.parse("2026-05-11T10:15:30")

    @Test
    fun `creates a normalized succeeded result snapshot`() {
        val snapshot =
            snapshot(
                externalTaskId = " external-123 ",
                assetSha256 = "A".repeat(64),
            )

        assertFalse(snapshot.id.isBlank())
        assertEquals(taskId, snapshot.mediaProcessingTaskId)
        assertEquals(contentId, snapshot.contentId)
        assertEquals("external-123", snapshot.externalTaskId)
        assertEquals(MediaProcessingResultStatus.SUCCEEDED, snapshot.resultStatus)
        assertEquals("a".repeat(64), snapshot.assetSha256)
        assertEquals("s3://content-studio/assets/external-123.mp4", snapshot.assetLocation)
        assertEquals(completedAt, snapshot.completedAt)
    }

    @Test
    fun `result snapshot remains a json-backed value not a separate table entity`() {
        val type = MediaProcessingResultSnapshot::class.java

        assertNull(type.getAnnotation(Entity::class.java))
        assertNull(type.getAnnotation(Table::class.java))
        assertFalse(ValueObject::class.java.isAssignableFrom(type))
    }

    @Test
    fun `converter round trips result snapshot as json`() {
        val converter = MediaProcessingResultSnapshotConverter()
        val snapshot = snapshot()

        val json = converter.convertToDatabaseColumn(snapshot)
        val restored = converter.convertToEntityAttribute(json)

        assertEquals(snapshot, restored)
    }

    @Test
    fun `blank external task id is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            snapshot(externalTaskId = "   ")
        }
    }

    @Test
    fun `invalid asset sha is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            snapshot(assetSha256 = "not-a-sha")
        }
    }

    @Test
    fun `blank asset location is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            snapshot(assetLocation = "   ")
        }
    }

    @Test
    fun `content id contributes to snapshot identity`() {
        val first = snapshot(contentId = UUID.fromString("00000000-0000-0000-0000-000000000301"))
        val second = snapshot(contentId = UUID.fromString("00000000-0000-0000-0000-000000000302"))

        assertFalse(first.id == second.id)
    }

    private fun snapshot(
        contentId: UUID = this.contentId,
        externalTaskId: String = "external-123",
        assetSha256: String = "a".repeat(64),
        assetLocation: String = "s3://content-studio/assets/external-123.mp4",
    ) = MediaProcessingResultSnapshot.create(
        mediaProcessingTaskId = taskId,
        contentId = contentId,
        externalTaskId = externalTaskId,
        resultStatus = MediaProcessingResultStatus.SUCCEEDED,
        assetSha256 = assetSha256,
        assetLocation = assetLocation,
        completedAt = completedAt,
        now = completedAt,
    )
}
