package com.only4.cap4k.reference.contentstudio.domain.services.paid.publication

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaidPublicationEligibilityServiceTest {
    private val service = PaidPublicationEligibilityService()

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

    @Test
    fun `paid content is not eligible before publication readiness`() {
        val result = service.decide(
            content(
                releasePolicy = ReleasePolicy.PAID,
                reviewStatus = ReviewStatus.PENDING,
                mediaReadyAt = LocalDateTime.of(2026, 5, 17, 10, 0),
            ),
            existingTask = null,
        )

        assertEquals(PaidPublicationEligibilityService.Decision.NotPublicationReady, result)
    }

    @Test
    fun `published paid content is not eligible to start again`() {
        val result = service.decide(
            content(
                releasePolicy = ReleasePolicy.PAID,
                contentStatus = ContentStatus.PUBLISHED,
            ),
            existingTask = null,
        )

        assertEquals(PaidPublicationEligibilityService.Decision.AlreadyPublished, result)
    }

    private fun paidReadyContent(): Content =
        content(releasePolicy = ReleasePolicy.PAID)

    private fun immediateReadyContent(): Content =
        content(releasePolicy = ReleasePolicy.IMMEDIATE)

    private fun content(
        releasePolicy: ReleasePolicy,
        reviewStatus: ReviewStatus = ReviewStatus.APPROVED,
        contentStatus: ContentStatus = ContentStatus.DRAFT,
        mediaReadyAt: LocalDateTime? = LocalDateTime.of(2026, 5, 17, 10, 0),
    ): Content {
        val now = LocalDateTime.of(2026, 5, 17, 9, 0)
        return Content(
            id = ContentId.new(),
            title = "Publication ready content",
            body = "Approved content with media ready",
            mediaSourceKey = "media/ready-content.mp4",
            reviewStatus = reviewStatus,
            contentStatus = contentStatus,
            releasePolicy = releasePolicy,
            reviewerId = ReviewerId.parse(ContentId.new().toString()),
            reviewedAt = now.plusMinutes(30),
            publishedAt = if (contentStatus == ContentStatus.PUBLISHED) now.plusHours(2) else null,
            mediaReadyAt = mediaReadyAt,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }

    private fun runningTask(): PaidPublicationTask {
        val now = LocalDateTime.of(2026, 5, 17, 9, 0)
        return PaidPublicationTask(
            id = PaidPublicationTaskId.new(),
            contentId = ContentId.new(),
            paidPublicationStatus = PaidPublicationStatus.RUNNING,
            publicationSagaId = "saga-123",
            payoutHoldStatus = PayoutHoldStatus.NONE,
            payoutHoldId = null,
            entitlementPlanStatus = EntitlementPlanStatus.NONE,
            entitlementPlanId = null,
            startedAt = now.plusHours(1),
            publishedAt = null,
            completedAt = null,
            failedAt = null,
            failedReason = null,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }
}
