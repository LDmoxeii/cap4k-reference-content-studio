package com.only4.cap4k.reference.contentstudio.domain.services.paid.publication

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus
import java.time.LocalDateTime
import java.util.UUID
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

    private fun paidReadyContent(): Content =
        content(releasePolicy = ReleasePolicy.PAID)

    private fun immediateReadyContent(): Content =
        content(releasePolicy = ReleasePolicy.IMMEDIATE)

    private fun content(releasePolicy: ReleasePolicy): Content {
        val now = LocalDateTime.of(2026, 5, 17, 9, 0)
        return Content(
            id = UUID.randomUUID(),
            title = "Publication ready content",
            body = "Approved content with media ready",
            mediaSourceKey = "media/ready-content.mp4",
            reviewStatus = ReviewStatus.APPROVED,
            contentStatus = ContentStatus.DRAFT,
            releasePolicy = releasePolicy,
            reviewerId = UUID.randomUUID(),
            reviewedAt = now.plusMinutes(30),
            publishedAt = null,
            mediaReadyAt = now.plusHours(1),
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }

    private fun runningTask(): PaidPublicationTask {
        val now = LocalDateTime.of(2026, 5, 17, 9, 0)
        return PaidPublicationTask(
            id = UUID.randomUUID(),
            contentId = UUID.randomUUID(),
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
