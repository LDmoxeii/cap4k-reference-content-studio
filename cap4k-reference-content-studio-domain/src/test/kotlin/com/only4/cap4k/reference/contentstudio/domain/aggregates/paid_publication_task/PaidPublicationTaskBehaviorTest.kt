package com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task

import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.factory.PaidPublicationTaskFactory
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PaidPublicationTaskBehaviorTest {

    @Test
    fun `record saga start is idempotent for same saga id`() {
        val task = newTask()
        val startedAt = LocalDateTime.of(2026, 5, 17, 10, 0)

        task.recordSagaStarted("saga-123", startedAt)
        task.recordSagaStarted("saga-123", startedAt.plusMinutes(5))

        assertEquals("saga-123", task.publicationSagaId)
        assertEquals(PaidPublicationStatus.RUNNING, task.paidPublicationStatus)
        assertEquals(startedAt, task.startedAt)
        assertThrows(IllegalStateException::class.java) {
            task.recordSagaStarted("saga-456", startedAt.plusMinutes(10))
        }
    }

    @Test
    fun `record payout hold reservation and release are idempotent`() {
        val task = newTask()

        task.recordPayoutHoldReserved("hold-123")
        task.recordPayoutHoldReserved("hold-123")
        task.recordPayoutHoldReleased()
        task.recordPayoutHoldReleased()
        task.recordPayoutHoldReserved("hold-123")

        assertEquals("hold-123", task.payoutHoldId)
        assertEquals(PayoutHoldStatus.RELEASED, task.payoutHoldStatus)
        assertThrows(IllegalStateException::class.java) {
            task.recordPayoutHoldReserved("hold-456")
        }
    }

    @Test
    fun `activated entitlement cannot be cancelled automatically`() {
        val task = newTask()

        task.recordEntitlementPlanCreated("plan-123")
        task.recordEntitlementPlanActivated()

        assertThrows(IllegalStateException::class.java) {
            task.recordEntitlementPlanCancelled()
        }
        assertEquals("plan-123", task.entitlementPlanId)
        assertEquals(EntitlementPlanStatus.ACTIVATED, task.entitlementPlanStatus)
    }

    @Test
    fun `published task can require operator repair`() {
        val task = newTask()
        val publishedAt = LocalDateTime.of(2026, 5, 17, 11, 0)
        val failedAt = LocalDateTime.of(2026, 5, 17, 11, 30)

        task.markPublished(publishedAt)
        task.markRequiresOperatorRepair("entitlement callback failed", failedAt)

        assertEquals(PaidPublicationStatus.REQUIRES_OPERATOR_REPAIR, task.paidPublicationStatus)
        assertEquals(publishedAt, task.publishedAt)
        assertEquals(failedAt, task.failedAt)
        assertEquals("entitlement callback failed", task.failedReason)
    }

    @Test
    fun `factory creates pending task with empty external state`() {
        val now = LocalDateTime.of(2026, 5, 17, 9, 30)
        val id = UUID.randomUUID()
        val contentId = UUID.randomUUID()

        val task = PaidPublicationTaskFactory().create(
            PaidPublicationTaskFactory.Payload(
                id = id,
                contentId = contentId,
                now = now,
            )
        )

        assertEquals(id, task.id)
        assertEquals(contentId, task.contentId)
        assertEquals(PaidPublicationStatus.PENDING, task.paidPublicationStatus)
        assertEquals(PayoutHoldStatus.NONE, task.payoutHoldStatus)
        assertEquals(EntitlementPlanStatus.NONE, task.entitlementPlanStatus)
        assertNull(task.publicationSagaId)
        assertNull(task.payoutHoldId)
        assertNull(task.entitlementPlanId)
        assertNull(task.startedAt)
        assertNull(task.publishedAt)
        assertNull(task.completedAt)
        assertNull(task.failedAt)
        assertNull(task.failedReason)
        assertEquals(now, task.dbCreatedAt)
        assertEquals(now, task.dbUpdatedAt)
    }

    private fun newTask(
        paidPublicationStatus: PaidPublicationStatus = PaidPublicationStatus.PENDING,
        publicationSagaId: String? = null,
        payoutHoldStatus: PayoutHoldStatus = PayoutHoldStatus.NONE,
        payoutHoldId: String? = null,
        entitlementPlanStatus: EntitlementPlanStatus = EntitlementPlanStatus.NONE,
        entitlementPlanId: String? = null,
        startedAt: LocalDateTime? = null,
        publishedAt: LocalDateTime? = null,
        completedAt: LocalDateTime? = null,
        failedAt: LocalDateTime? = null,
        failedReason: String? = null,
    ): PaidPublicationTask {
        val now = LocalDateTime.of(2026, 5, 17, 9, 0)
        return PaidPublicationTask(
            id = UUID.randomUUID(),
            contentId = UUID.randomUUID(),
            paidPublicationStatus = paidPublicationStatus,
            publicationSagaId = publicationSagaId,
            payoutHoldStatus = payoutHoldStatus,
            payoutHoldId = payoutHoldId,
            entitlementPlanStatus = entitlementPlanStatus,
            entitlementPlanId = entitlementPlanId,
            startedAt = startedAt,
            publishedAt = publishedAt,
            completedAt = completedAt,
            failedAt = failedAt,
            failedReason = failedReason,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }
}
