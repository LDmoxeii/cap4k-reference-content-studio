package com.only4.cap4k.reference.contentstudio.domain.services

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class PublicationEligibilityDomainServiceTest {
    private val service = PublicationEligibilityDomainService()

    @Test
    fun `approved content with succeeded media processing is eligible`() {
        val contentId = UUID.randomUUID()
        val content = newContent(contentId = contentId, reviewStatus = ReviewStatus.APPROVED)
        val task = newTask(contentId = contentId, processingStatus = MediaProcessingStatus.SUCCEEDED)

        assertEquals(PublicationEligibilityDecision.Eligible, service.evaluate(content = content, task = task))
    }

    @Test
    fun `pending review content is not approved`() {
        val contentId = UUID.randomUUID()
        val content = newContent(contentId = contentId, reviewStatus = ReviewStatus.PENDING)
        val task = newTask(contentId = contentId, processingStatus = MediaProcessingStatus.SUCCEEDED)

        assertEquals(PublicationEligibilityDecision.ContentNotApproved, service.evaluate(content = content, task = task))
    }

    @Test
    fun `submitted media processing task has not succeeded`() {
        val contentId = UUID.randomUUID()
        val content = newContent(contentId = contentId, reviewStatus = ReviewStatus.APPROVED)
        val task = newTask(contentId = contentId, processingStatus = MediaProcessingStatus.SUBMITTED)

        assertEquals(PublicationEligibilityDecision.MediaProcessingNotSucceeded, service.evaluate(content = content, task = task))
    }

    @Test
    fun `media processing task must belong to content`() {
        val content = newContent(
            contentId = UUID.randomUUID(),
            reviewStatus = ReviewStatus.APPROVED,
        )
        val unrelatedTask = newTask(
            contentId = UUID.randomUUID(),
            processingStatus = MediaProcessingStatus.SUCCEEDED,
        )

        assertEquals(
            PublicationEligibilityDecision.TaskDoesNotBelongToContent,
            service.evaluate(content = content, task = unrelatedTask),
        )
    }

    @Test
    fun `release readiness must be satisfied`() {
        val contentId = UUID.randomUUID()
        val content = newContent(contentId = contentId, reviewStatus = ReviewStatus.APPROVED)
        val task = newTask(contentId = contentId, processingStatus = MediaProcessingStatus.SUCCEEDED)

        assertEquals(
            PublicationEligibilityDecision.ReleaseReadinessNotSatisfied,
            service.evaluate(content = content, task = task, releaseReadinessSatisfied = false),
        )
    }

    private fun newContent(
        contentId: UUID,
        reviewStatus: ReviewStatus,
        contentStatus: ContentStatus = ContentStatus.DRAFT,
    ): Content {
        val now = LocalDateTime.of(2026, 5, 9, 9, 0)
        return Content(
            id = contentId,
            title = "Draft title",
            body = "Draft body",
            mediaSourceKey = "media/source-key",
            reviewStatus = reviewStatus,
            contentStatus = contentStatus,
            releasePolicy = ReleasePolicy.IMMEDIATE,
            releaseWindowOpensAt = null,
            releaseWindowClosesAt = null,
            reviewerId = null,
            reviewedAt = null,
            publishedAt = null,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }

    private fun newTask(contentId: UUID, processingStatus: MediaProcessingStatus): MediaProcessingTask {
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
