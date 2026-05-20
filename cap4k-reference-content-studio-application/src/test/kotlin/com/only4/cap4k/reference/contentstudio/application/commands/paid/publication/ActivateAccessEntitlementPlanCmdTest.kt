package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ActivateAccessEntitlementPlanCmdTest {

    @Test
    fun `activated entitlement is idempotent only after paid publication is published`() {
        assertThrows(IllegalStateException::class.java) {
            ActivateAccessEntitlementPlanCmd.validateLoadedTaskForActivation(
                newTask(
                    paidPublicationStatus = PaidPublicationStatus.RUNNING,
                    entitlementPlanStatus = EntitlementPlanStatus.ACTIVATED,
                    completedAt = LocalDateTime.of(2026, 5, 17, 11, 30),
                )
            )
        }

        val response = ActivateAccessEntitlementPlanCmd.validateLoadedTaskForActivation(
            newTask(
                paidPublicationStatus = PaidPublicationStatus.PUBLISHED,
                entitlementPlanStatus = EntitlementPlanStatus.ACTIVATED,
                completedAt = LocalDateTime.of(2026, 5, 17, 11, 30),
            )
        )

        assertFalse(requireNotNull(response).activated)
    }

    @Test
    fun `activated entitlement idempotency requires completed time`() {
        assertThrows(IllegalStateException::class.java) {
            ActivateAccessEntitlementPlanCmd.validateLoadedTaskForActivation(
                newTask(
                    paidPublicationStatus = PaidPublicationStatus.PUBLISHED,
                    entitlementPlanStatus = EntitlementPlanStatus.ACTIVATED,
                )
            )
        }
    }

    @Test
    fun `created entitlement continues to external activation after validation`() {
        val response = ActivateAccessEntitlementPlanCmd.validateLoadedTaskForActivation(
            newTask(
                paidPublicationStatus = PaidPublicationStatus.PUBLISHED,
                entitlementPlanStatus = EntitlementPlanStatus.CREATED,
            )
        )

        assertNull(response)
    }

    private fun newTask(
        paidPublicationStatus: PaidPublicationStatus,
        entitlementPlanStatus: EntitlementPlanStatus,
        completedAt: LocalDateTime? = null,
    ): PaidPublicationTask {
        val now = LocalDateTime.of(2026, 5, 17, 9, 0)
        return PaidPublicationTask(
            id = UUID.randomUUID(),
            contentId = UUID.randomUUID(),
            paidPublicationStatus = paidPublicationStatus,
            publicationSagaId = "saga-123",
            payoutHoldStatus = PayoutHoldStatus.RESERVED,
            payoutHoldId = "hold-123",
            entitlementPlanStatus = entitlementPlanStatus,
            entitlementPlanId = "plan-123",
            startedAt = LocalDateTime.of(2026, 5, 17, 10, 0),
            publishedAt = if (paidPublicationStatus == PaidPublicationStatus.PUBLISHED) {
                LocalDateTime.of(2026, 5, 17, 11, 0)
            } else {
                null
            },
            completedAt = completedAt,
            failedAt = null,
            failedReason = null,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }
}
