package com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.factory.PaidPublicationTaskFactory
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PaidPublicationTaskBehaviorTest {

    @Test
    fun `saga can only start pending task and is idempotent for same saga id`() {
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
        assertThrows(IllegalStateException::class.java) {
            newTask(paidPublicationStatus = PaidPublicationStatus.PUBLISHED)
                .recordSagaStarted("saga-789", startedAt)
        }
        assertThrows(IllegalStateException::class.java) {
            newTask(paidPublicationStatus = PaidPublicationStatus.FAILED)
                .recordSagaStarted("saga-789", startedAt)
        }
        assertThrows(IllegalStateException::class.java) {
            newTask(paidPublicationStatus = PaidPublicationStatus.REQUIRES_OPERATOR_REPAIR)
                .recordSagaStarted("saga-789", startedAt)
        }
    }

    @Test
    fun `payout hold can only be reserved after saga starts`() {
        val task = newTask()

        assertThrows(IllegalStateException::class.java) {
            task.recordPayoutHoldReserved("hold-123")
        }

        task.recordSagaStarted("saga-123", LocalDateTime.of(2026, 5, 17, 10, 0))
        task.recordPayoutHoldReserved("hold-123")
        task.recordPayoutHoldReserved("hold-123")

        assertEquals("hold-123", task.payoutHoldId)
        assertEquals(PayoutHoldStatus.RESERVED, task.payoutHoldStatus)
        assertThrows(IllegalStateException::class.java) {
            task.recordPayoutHoldReserved("hold-456")
        }
    }

    @Test
    fun `payout hold release only changes reserved hold`() {
        val task = runningTaskWithReservedHold()
        val alreadyReleasedTask = runningTask(
            payoutHoldStatus = PayoutHoldStatus.RELEASED,
            payoutHoldId = "hold-456",
        )

        task.recordPayoutHoldReleased()
        task.recordPayoutHoldReleased()
        alreadyReleasedTask.recordPayoutHoldReleased()

        assertEquals("hold-123", task.payoutHoldId)
        assertEquals(PayoutHoldStatus.RELEASED, task.payoutHoldStatus)
        assertEquals("hold-456", alreadyReleasedTask.payoutHoldId)
        assertEquals(PayoutHoldStatus.RELEASED, alreadyReleasedTask.payoutHoldStatus)
        newTask().recordPayoutHoldReleased()
        assertThrows(IllegalStateException::class.java) {
            runningTask(
                payoutHoldStatus = PayoutHoldStatus.CAPTURED,
                payoutHoldId = "hold-123",
            ).recordPayoutHoldReleased()
        }
    }

    @Test
    fun `entitlement plan requires reserved payout hold`() {
        assertThrows(IllegalStateException::class.java) {
            runningTask().recordEntitlementPlanCreated("plan-123")
        }
        assertThrows(IllegalStateException::class.java) {
            runningTask(payoutHoldStatus = PayoutHoldStatus.RELEASED, payoutHoldId = "hold-123")
                .recordEntitlementPlanCreated("plan-123")
        }

        val task = runningTaskWithReservedHold()

        task.recordEntitlementPlanCreated("plan-123")
        task.recordEntitlementPlanCreated("plan-123")

        assertEquals("plan-123", task.entitlementPlanId)
        assertEquals(EntitlementPlanStatus.CREATED, task.entitlementPlanStatus)
        assertThrows(IllegalStateException::class.java) {
            task.recordEntitlementPlanCreated("plan-456")
        }
    }

    @Test
    fun `created entitlement plan can be cancelled but activated plan cannot`() {
        val task = runningTaskWithHoldAndPlan()
        val alreadyCancelledTask = runningTask(
            payoutHoldStatus = PayoutHoldStatus.RESERVED,
            payoutHoldId = "hold-456",
            entitlementPlanStatus = EntitlementPlanStatus.CANCELLED,
            entitlementPlanId = "plan-456",
        )

        task.recordEntitlementPlanCancelled()
        task.recordEntitlementPlanCancelled()
        alreadyCancelledTask.recordEntitlementPlanCancelled()

        assertEquals(EntitlementPlanStatus.CANCELLED, task.entitlementPlanStatus)
        assertEquals("plan-456", alreadyCancelledTask.entitlementPlanId)
        assertEquals(EntitlementPlanStatus.CANCELLED, alreadyCancelledTask.entitlementPlanStatus)
        newTask().recordEntitlementPlanCancelled()
        assertThrows(IllegalStateException::class.java) {
            publishedTaskWithActivatedEntitlement().recordEntitlementPlanCancelled()
        }
    }

    @Test
    fun `paid publication can be marked published only after hold and plan are ready`() {
        val publishedAt = LocalDateTime.of(2026, 5, 17, 11, 0)

        assertThrows(IllegalStateException::class.java) {
            runningTask().markPublished(publishedAt)
        }
        assertThrows(IllegalStateException::class.java) {
            runningTaskWithReservedHold().markPublished(publishedAt)
        }

        val task = runningTaskWithHoldAndPlan()

        task.markPublished(publishedAt)
        task.markPublished(publishedAt.plusMinutes(5))

        assertEquals(PaidPublicationStatus.PUBLISHED, task.paidPublicationStatus)
        assertEquals(publishedAt, task.publishedAt)
    }

    @Test
    fun `published duplicate is only no-op when published state is internally valid`() {
        val publishedAt = LocalDateTime.of(2026, 5, 17, 11, 0)

        assertThrows(IllegalStateException::class.java) {
            newTask(
                paidPublicationStatus = PaidPublicationStatus.PUBLISHED,
                entitlementPlanStatus = EntitlementPlanStatus.CREATED,
                entitlementPlanId = "plan-123",
                publishedAt = publishedAt,
            ).markPublished(publishedAt.plusMinutes(5))
        }
        assertThrows(IllegalStateException::class.java) {
            newTask(
                paidPublicationStatus = PaidPublicationStatus.PUBLISHED,
                payoutHoldStatus = PayoutHoldStatus.RESERVED,
                payoutHoldId = "hold-123",
                publishedAt = publishedAt,
            ).markPublished(publishedAt.plusMinutes(5))
        }
        assertThrows(IllegalStateException::class.java) {
            newTask(
                paidPublicationStatus = PaidPublicationStatus.PUBLISHED,
                payoutHoldStatus = PayoutHoldStatus.RESERVED,
                payoutHoldId = "hold-123",
                entitlementPlanStatus = EntitlementPlanStatus.CREATED,
                entitlementPlanId = "plan-123",
            ).markPublished(publishedAt.plusMinutes(5))
        }

        val validPublishedTask = publishedTaskWithCreatedEntitlement()

        validPublishedTask.markPublished(publishedAt.plusMinutes(5))

        assertEquals(publishedAt, validPublishedTask.publishedAt)
    }

    @Test
    fun `pending failed and repair tasks cannot be marked published`() {
        val publishedAt = LocalDateTime.of(2026, 5, 17, 11, 0)

        assertThrows(IllegalStateException::class.java) {
            newTask(paidPublicationStatus = PaidPublicationStatus.PENDING).markPublished(publishedAt)
        }
        assertThrows(IllegalStateException::class.java) {
            newTask(paidPublicationStatus = PaidPublicationStatus.FAILED).markPublished(publishedAt)
        }
        assertThrows(IllegalStateException::class.java) {
            newTask(paidPublicationStatus = PaidPublicationStatus.REQUIRES_OPERATOR_REPAIR).markPublished(publishedAt)
        }
    }

    @Test
    fun `entitlement activation completes published paid publication and sets completed at`() {
        val task = publishedTaskWithCreatedEntitlement()
        val completedAt = LocalDateTime.of(2026, 5, 17, 11, 30)

        task.recordEntitlementPlanActivated(completedAt)
        task.recordEntitlementPlanActivated(completedAt.plusMinutes(5))

        assertEquals(EntitlementPlanStatus.ACTIVATED, task.entitlementPlanStatus)
        assertEquals(completedAt, task.completedAt)
    }

    @Test
    fun `duplicate entitlement activation requires completed state to be internally complete`() {
        val completedAt = LocalDateTime.of(2026, 5, 17, 11, 30)
        val completedTask = publishedTaskWithActivatedEntitlement()

        completedTask.recordEntitlementPlanActivated(completedAt.plusMinutes(5))

        assertEquals(EntitlementPlanStatus.ACTIVATED, completedTask.entitlementPlanStatus)
        assertEquals(completedAt, completedTask.completedAt)
        assertThrows(IllegalStateException::class.java) {
            newTask(
                paidPublicationStatus = PaidPublicationStatus.PUBLISHED,
                publicationSagaId = "saga-123",
                payoutHoldStatus = PayoutHoldStatus.RESERVED,
                payoutHoldId = "hold-123",
                entitlementPlanStatus = EntitlementPlanStatus.ACTIVATED,
                entitlementPlanId = "plan-123",
                startedAt = LocalDateTime.of(2026, 5, 17, 10, 0),
                publishedAt = LocalDateTime.of(2026, 5, 17, 11, 0),
            ).recordEntitlementPlanActivated(completedAt)
        }
    }

    @Test
    fun `activation before published is rejected`() {
        val task = runningTaskWithHoldAndPlan()

        assertThrows(IllegalStateException::class.java) {
            task.recordEntitlementPlanActivated(LocalDateTime.of(2026, 5, 17, 11, 30))
        }
    }

    @Test
    fun `mark failed rejects published task and repair handles published task`() {
        val task = publishedTaskWithCreatedEntitlement()
        val publishedAt = requireNotNull(task.publishedAt)
        val failedAt = LocalDateTime.of(2026, 5, 17, 11, 30)

        assertThrows(IllegalStateException::class.java) {
            task.markFailed("entitlement activation failed after publication", failedAt)
        }

        task.markRequiresOperatorRepair("entitlement activation failed after publication", failedAt)

        assertEquals(PaidPublicationStatus.REQUIRES_OPERATOR_REPAIR, task.paidPublicationStatus)
        assertEquals(publishedAt, task.publishedAt)
        assertEquals(failedAt, task.failedAt)
        assertEquals("entitlement activation failed after publication", task.failedReason)
    }

    @Test
    fun `mark failed is idempotent for same pre publication failure`() {
        val task = runningTaskWithReservedHold()
        val failedAt = LocalDateTime.of(2026, 5, 17, 10, 30)

        assertThrows(IllegalStateException::class.java) {
            task.markFailed(" ", failedAt)
        }

        task.markFailed("payout hold reservation failed", failedAt)
        task.markFailed("payout hold reservation failed", failedAt.plusMinutes(5))

        assertEquals(PaidPublicationStatus.FAILED, task.paidPublicationStatus)
        assertEquals(failedAt, task.failedAt)
        assertEquals("payout hold reservation failed", task.failedReason)
        assertThrows(IllegalStateException::class.java) {
            task.markFailed("different failure", failedAt.plusMinutes(10))
        }
    }

    @Test
    fun `mark requires operator repair rejects blank reason`() {
        assertThrows(IllegalStateException::class.java) {
            publishedTaskWithCreatedEntitlement()
                .markRequiresOperatorRepair(" ", LocalDateTime.of(2026, 5, 17, 11, 30))
        }
    }

    @Test
    fun `factory creates pending task with empty external state`() {
        val now = LocalDateTime.of(2026, 5, 17, 9, 30)
        val contentId = ContentId.new()

        val task = PaidPublicationTaskFactory().create(
            PaidPublicationTaskFactory.Payload(
                contentId = contentId,
                now = now,
            )
        )

        assertNotNull(task.id)
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

    private fun runningTask(
        payoutHoldStatus: PayoutHoldStatus = PayoutHoldStatus.NONE,
        payoutHoldId: String? = null,
        entitlementPlanStatus: EntitlementPlanStatus = EntitlementPlanStatus.NONE,
        entitlementPlanId: String? = null,
    ): PaidPublicationTask =
        newTask(
            paidPublicationStatus = PaidPublicationStatus.RUNNING,
            publicationSagaId = "saga-123",
            payoutHoldStatus = payoutHoldStatus,
            payoutHoldId = payoutHoldId,
            entitlementPlanStatus = entitlementPlanStatus,
            entitlementPlanId = entitlementPlanId,
            startedAt = LocalDateTime.of(2026, 5, 17, 10, 0),
        )

    private fun runningTaskWithReservedHold(): PaidPublicationTask =
        runningTask(
            payoutHoldStatus = PayoutHoldStatus.RESERVED,
            payoutHoldId = "hold-123",
        )

    private fun runningTaskWithHoldAndPlan(): PaidPublicationTask =
        runningTask(
            payoutHoldStatus = PayoutHoldStatus.RESERVED,
            payoutHoldId = "hold-123",
            entitlementPlanStatus = EntitlementPlanStatus.CREATED,
            entitlementPlanId = "plan-123",
        )

    private fun publishedTaskWithCreatedEntitlement(): PaidPublicationTask =
        newTask(
            paidPublicationStatus = PaidPublicationStatus.PUBLISHED,
            publicationSagaId = "saga-123",
            payoutHoldStatus = PayoutHoldStatus.RESERVED,
            payoutHoldId = "hold-123",
            entitlementPlanStatus = EntitlementPlanStatus.CREATED,
            entitlementPlanId = "plan-123",
            startedAt = LocalDateTime.of(2026, 5, 17, 10, 0),
            publishedAt = LocalDateTime.of(2026, 5, 17, 11, 0),
        )

    private fun publishedTaskWithActivatedEntitlement(): PaidPublicationTask =
        newTask(
            paidPublicationStatus = PaidPublicationStatus.PUBLISHED,
            publicationSagaId = "saga-123",
            payoutHoldStatus = PayoutHoldStatus.RESERVED,
            payoutHoldId = "hold-123",
            entitlementPlanStatus = EntitlementPlanStatus.ACTIVATED,
            entitlementPlanId = "plan-123",
            startedAt = LocalDateTime.of(2026, 5, 17, 10, 0),
            publishedAt = LocalDateTime.of(2026, 5, 17, 11, 0),
            completedAt = LocalDateTime.of(2026, 5, 17, 11, 30),
        )

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
            id = PaidPublicationTaskId.new(),
            contentId = ContentId.new(),
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
