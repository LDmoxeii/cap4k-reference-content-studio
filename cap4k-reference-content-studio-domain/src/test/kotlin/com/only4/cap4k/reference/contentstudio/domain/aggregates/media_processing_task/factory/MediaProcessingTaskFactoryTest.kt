package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.factory

import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import java.time.LocalDateTime
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class MediaProcessingTaskFactoryTest {

    private val factory = MediaProcessingTaskFactory()

    @Test
    fun `create builds media processing task aggregate from payload`() {
        val now = LocalDateTime.of(2026, 5, 9, 9, 0)
        val payload =
            MediaProcessingTaskFactory.Payload(
                contentId = ContentId.new(),
                externalTaskId = "external-123",
                processingStatus = MediaProcessingStatus.SUBMITTED,
                resultSnapshot = null,
                dbCreatedAt = now,
                dbUpdatedAt = now.plusMinutes(5),
            )

        val task = factory.create(payload)

        assertNotNull(task.id)
        assertEquals(payload.contentId, task.contentId)
        assertEquals(payload.externalTaskId, task.externalTaskId)
        assertEquals(payload.processingStatus, task.processingStatus)
        assertEquals(payload.resultSnapshot, task.resultSnapshot)
        assertEquals(payload.dbCreatedAt, task.dbCreatedAt)
        assertEquals(payload.dbUpdatedAt, task.dbUpdatedAt)
    }
}
