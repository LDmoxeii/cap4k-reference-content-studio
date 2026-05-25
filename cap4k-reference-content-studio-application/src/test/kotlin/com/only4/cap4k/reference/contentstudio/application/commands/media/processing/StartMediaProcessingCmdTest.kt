package com.only4.cap4k.reference.contentstudio.application.commands.media.processing

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StartMediaProcessingCmdTest {

    @Test
    fun `submitted task retreats without replacing external task id`() {
        val task = task(
            externalTaskId = "external-123",
            processingStatus = MediaProcessingStatus.SUBMITTED,
        )

        val decision = StartMediaProcessingCmd.decideLoadedTask(task)
        val response = StartMediaProcessingCmd.responseForDecision(decision)

        assertEquals(StartMediaProcessingCmd.Decision.AlreadySubmitted, decision)
        assertEquals(StartMediaProcessingCmd.Decision.AlreadySubmitted, response.decision)
        assertEquals("external-123", task.externalTaskId)
        assertEquals(MediaProcessingStatus.SUBMITTED, task.processingStatus)
    }

    @Test
    fun `succeeded task retreats without replacing external task id`() {
        val task = task(
            externalTaskId = "external-123",
            processingStatus = MediaProcessingStatus.SUCCEEDED,
        )

        val decision = StartMediaProcessingCmd.decideLoadedTask(task)
        val response = StartMediaProcessingCmd.responseForDecision(decision)

        assertEquals(StartMediaProcessingCmd.Decision.AlreadySucceeded, decision)
        assertEquals(StartMediaProcessingCmd.Decision.AlreadySucceeded, response.decision)
        assertEquals("external-123", task.externalTaskId)
        assertEquals(MediaProcessingStatus.SUCCEEDED, task.processingStatus)
    }

    @Test
    fun `pending task can start external media processing`() {
        val decision = StartMediaProcessingCmd.decideLoadedTask(task())

        assertEquals(StartMediaProcessingCmd.Decision.ShouldStart, decision)
    }

    private fun task(
        externalTaskId: String? = null,
        processingStatus: MediaProcessingStatus = MediaProcessingStatus.PENDING,
    ): MediaProcessingTask {
        val now = LocalDateTime.of(2026, 5, 17, 9, 0)
        return MediaProcessingTask(
            id = MediaProcessingTaskId.new(),
            contentId = ContentId.new(),
            externalTaskId = externalTaskId,
            processingStatus = processingStatus,
            resultSnapshot = null,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }
}
