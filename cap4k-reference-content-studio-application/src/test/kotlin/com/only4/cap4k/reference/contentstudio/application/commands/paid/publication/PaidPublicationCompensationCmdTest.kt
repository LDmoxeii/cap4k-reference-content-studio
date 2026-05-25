package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PaidPublicationCompensationCmdTest {

    @Test
    fun `release payout hold no-ops when no hold was reserved`() {
        val response = ReleasePayoutHoldIfReservedCmd.validateLoadedTaskForRelease(task())

        assertEquals(ReleasePayoutHoldIfReservedCmd.Decision.NoPayoutHold, requireNotNull(response).decision)
        assertFalse(response.released)
    }

    @Test
    fun `release payout hold continues only for reserved hold`() {
        val response = ReleasePayoutHoldIfReservedCmd.validateLoadedTaskForRelease(
            task(
                payoutHoldStatus = PayoutHoldStatus.RESERVED,
                payoutHoldId = "hold-123",
            )
        )

        assertNull(response)
    }

    @Test
    fun `captured payout hold marks operator repair instead of external release`() {
        val task = task(
            payoutHoldStatus = PayoutHoldStatus.CAPTURED,
            payoutHoldId = "hold-123",
        )

        val response = ReleasePayoutHoldIfReservedCmd.applyCapturedHoldRepair(
            task = task,
            failedAt = LocalDateTime.of(2026, 5, 17, 12, 0),
        )

        assertFalse(response.released)
        assertEquals(ReleasePayoutHoldIfReservedCmd.Decision.CapturedRequiresOperatorRepair, response.decision)
        assertEquals(PaidPublicationStatus.REQUIRES_OPERATOR_REPAIR, task.paidPublicationStatus)
        assertEquals("Payout hold is captured and cannot be released automatically.", task.failedReason)
    }

    @Test
    fun `cancel entitlement no-ops when no plan was created`() {
        val response = CancelEntitlementPlanIfCreatedCmd.validateLoadedTaskForCancellation(task())

        assertEquals(CancelEntitlementPlanIfCreatedCmd.Decision.NoEntitlementPlan, requireNotNull(response).decision)
        assertFalse(response.cancelled)
    }

    @Test
    fun `cancel entitlement continues only for created plan`() {
        val response = CancelEntitlementPlanIfCreatedCmd.validateLoadedTaskForCancellation(
            task(
                entitlementPlanStatus = EntitlementPlanStatus.CREATED,
                entitlementPlanId = "plan-123",
            )
        )

        assertNull(response)
    }

    @Test
    fun `activated entitlement marks operator repair instead of external cancellation`() {
        val task = task(
            paidPublicationStatus = PaidPublicationStatus.PUBLISHED,
            entitlementPlanStatus = EntitlementPlanStatus.ACTIVATED,
            entitlementPlanId = "plan-123",
            publishedAt = LocalDateTime.of(2026, 5, 17, 11, 0),
            completedAt = LocalDateTime.of(2026, 5, 17, 11, 30),
        )

        val response = CancelEntitlementPlanIfCreatedCmd.applyActivatedPlanRepair(
            task = task,
            failedAt = LocalDateTime.of(2026, 5, 17, 12, 0),
        )

        assertFalse(response.cancelled)
        assertEquals(CancelEntitlementPlanIfCreatedCmd.Decision.ActivatedRequiresOperatorRepair, response.decision)
        assertEquals(PaidPublicationStatus.REQUIRES_OPERATOR_REPAIR, task.paidPublicationStatus)
        assertEquals("Entitlement plan is activated and cannot be cancelled automatically.", task.failedReason)
    }

    private fun task(
        paidPublicationStatus: PaidPublicationStatus = PaidPublicationStatus.RUNNING,
        payoutHoldStatus: PayoutHoldStatus = PayoutHoldStatus.NONE,
        payoutHoldId: String? = null,
        entitlementPlanStatus: EntitlementPlanStatus = EntitlementPlanStatus.NONE,
        entitlementPlanId: String? = null,
        publishedAt: LocalDateTime? = null,
        completedAt: LocalDateTime? = null,
    ): PaidPublicationTask {
        val now = LocalDateTime.of(2026, 5, 17, 9, 0)
        return PaidPublicationTask(
            id = PaidPublicationTaskId.new(),
            contentId = ContentId.new(),
            paidPublicationStatus = paidPublicationStatus,
            publicationSagaId = "saga-123",
            payoutHoldStatus = payoutHoldStatus,
            payoutHoldId = payoutHoldId,
            entitlementPlanStatus = entitlementPlanStatus,
            entitlementPlanId = entitlementPlanId,
            startedAt = LocalDateTime.of(2026, 5, 17, 10, 0),
            publishedAt = publishedAt,
            completedAt = completedAt,
            failedAt = null,
            failedReason = null,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }
}
