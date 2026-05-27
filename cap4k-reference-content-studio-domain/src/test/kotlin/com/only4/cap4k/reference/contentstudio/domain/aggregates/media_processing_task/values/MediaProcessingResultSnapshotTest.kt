package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values

import com.only4.cap4k.ddd.core.domain.aggregate.ValueObject
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingResultStatus
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class MediaProcessingResultSnapshotTest {
    private val taskId = MediaProcessingTaskId.new()
    private val contentId = ContentId.new()
    private val completedAt = LocalDateTime.parse("2026-05-11T10:15:30")

    @Test
    fun `generated result snapshot is a json-backed value carrier`() {
        val snapshot = snapshot()

        assertEquals(taskId, snapshot.mediaProcessingTaskId)
        assertEquals(contentId, snapshot.contentId)
        assertEquals("external-123", snapshot.externalTaskId)
        assertEquals(MediaProcessingResultStatus.SUCCEEDED, snapshot.resultStatus)
        assertEquals("a".repeat(64), snapshot.assetSha256)
        assertEquals("s3://content-studio/assets/external-123.mp4", snapshot.assetLocation)
        assertEquals(completedAt, snapshot.completedAt)
    }

    @Test
    fun `result snapshot remains json-backed and not a separate table entity`() {
        val type = MediaProcessingResultSnapshot::class.java

        assertNull(type.getAnnotation(Entity::class.java))
        assertNull(type.getAnnotation(Table::class.java))
        assertFalse(ValueObject::class.java.isAssignableFrom(type))
    }

    @Test
    fun `generated nested converter round trips result snapshot as json`() {
        val converter = MediaProcessingResultSnapshot.Converter()
        val snapshot = snapshot()

        val json = converter.convertToDatabaseColumn(snapshot)
        val restored = converter.convertToEntityAttribute(json)

        assertEquals(snapshot, restored)
    }

    private fun snapshot(
        contentId: ContentId = this.contentId,
        externalTaskId: String = "external-123",
        assetSha256: String = "a".repeat(64),
        assetLocation: String = "s3://content-studio/assets/external-123.mp4",
    ) = MediaProcessingResultSnapshot(
        mediaProcessingTaskId = taskId,
        contentId = contentId,
        externalTaskId = externalTaskId,
        resultStatus = MediaProcessingResultStatus.SUCCEEDED,
        assetSha256 = assetSha256,
        assetLocation = assetLocation,
        completedAt = completedAt,
        dbCreatedAt = completedAt,
        dbUpdatedAt = completedAt,
    )
}
