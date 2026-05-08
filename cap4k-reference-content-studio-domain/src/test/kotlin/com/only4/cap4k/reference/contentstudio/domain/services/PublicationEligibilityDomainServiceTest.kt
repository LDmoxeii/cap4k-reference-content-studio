package com.only4.cap4k.reference.contentstudio.domain.services

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class PublicationEligibilityDomainServiceTest {
    private val service = PublicationEligibilityDomainService()

    @Test
    fun `publish eligibility requires approved review and succeeded media processing`() {
        val contentId = UUID.randomUUID()
        val approvedContent = newContent(contentId = contentId, reviewStatus = ReviewStatus.APPROVED.name)
        val pendingReviewContent = newContent(contentId = contentId, reviewStatus = ReviewStatus.PENDING.name)
        val succeededTask = newTask(contentId = contentId, processingStatus = MediaProcessingStatus.SUCCEEDED.name)
        val submittedTask = newTask(contentId = contentId, processingStatus = MediaProcessingStatus.SUBMITTED.name)

        assertTrue(service.evaluate(content = approvedContent, task = succeededTask))
        assertFalse(service.evaluate(content = pendingReviewContent, task = succeededTask))
        assertFalse(service.evaluate(content = approvedContent, task = submittedTask))
    }

    @Test
    fun `publish eligibility requires media task to belong to the content`() {
        val content = newContent(
            contentId = UUID.randomUUID(),
            reviewStatus = ReviewStatus.APPROVED.name,
        )
        val unrelatedTask = newTask(
            contentId = UUID.randomUUID(),
            processingStatus = MediaProcessingStatus.SUCCEEDED.name,
        )

        assertFalse(service.evaluate(content = content, task = unrelatedTask))
    }

    private fun newContent(
        contentId: UUID,
        reviewStatus: String,
        contentStatus: String = ContentStatus.DRAFT.name,
    ): Content {
        val now = LocalDateTime.of(2026, 5, 9, 9, 0)
        return Content(
            id = contentId,
            title = "Draft title",
            body = "Draft body",
            mediaSourceKey = "media/source-key",
            reviewStatus = reviewStatus,
            contentStatus = contentStatus,
            reviewerId = null,
            reviewedAt = null,
            publishedAt = null,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }

    private fun newTask(contentId: UUID, processingStatus: String): MediaProcessingTask {
        val now = LocalDateTime.of(2026, 5, 9, 9, 0)
        return MediaProcessingTask(
            id = UUID.randomUUID(),
            contentId = contentId,
            externalTaskId = "external-123",
            processingStatus = processingStatus,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }
}
