package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

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
    fun `hash changes when asset sha changes`() {
        val first = snapshot(assetSha256 = "a".repeat(64))
        val second = snapshot(assetSha256 = "b".repeat(64))

        assertNotEquals(first.hash(), second.hash())
    }

    @Test
    fun `blank external task id is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            snapshot(externalTaskId = "   ")
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
