package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.factory

import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingStatus
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MediaProcessingTaskFactoryTest {

    private val factory = MediaProcessingTaskFactory()

    @Test
    fun `create builds media processing task aggregate from payload`() {
        val now = LocalDateTime.of(2026, 5, 9, 9, 0)
        val payload =
            MediaProcessingTaskFactory.Payload(
                id = UUID.randomUUID(),
                contentId = UUID.randomUUID(),
                externalTaskId = "external-123",
                processingStatus = MediaProcessingStatus.SUBMITTED.name,
                dbCreatedAt = now,
                dbUpdatedAt = now.plusMinutes(5),
            )

        val task = factory.create(payload)

        assertEquals(payload.id, task.id)
        assertEquals(payload.contentId, task.contentId)
        assertEquals(payload.externalTaskId, task.externalTaskId)
        assertEquals(payload.processingStatus, task.processingStatus)
        assertEquals(payload.dbCreatedAt, task.dbCreatedAt)
        assertEquals(payload.dbUpdatedAt, task.dbUpdatedAt)
    }
}
