package com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task

import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus
import java.time.LocalDateTime

fun PaidPublicationTask.recordSagaStarted(sagaId: String, now: LocalDateTime) {
    check(sagaId.isNotBlank()) {
        "Publication saga id must not be blank."
    }

    publicationSagaId?.let {
        check(it == sagaId) {
            "Cannot record a different publication saga id."
        }
        return
    }

    publicationSagaId = sagaId
    paidPublicationStatus = PaidPublicationStatus.RUNNING
    startedAt = now
}

fun PaidPublicationTask.recordPayoutHoldReserved(payoutHoldId: String) {
    check(payoutHoldId.isNotBlank()) {
        "Payout hold id must not be blank."
    }

    this.payoutHoldId?.let {
        check(it == payoutHoldId) {
            "Cannot record a different payout hold id."
        }
        return
    }

    this.payoutHoldId = payoutHoldId
    payoutHoldStatus = PayoutHoldStatus.RESERVED
}

fun PaidPublicationTask.recordPayoutHoldReleased() {
    when (payoutHoldStatus) {
        PayoutHoldStatus.NONE,
        PayoutHoldStatus.RELEASED -> return

        PayoutHoldStatus.CAPTURED -> error("Cannot release a captured payout hold.")

        PayoutHoldStatus.RESERVED -> payoutHoldStatus = PayoutHoldStatus.RELEASED
    }
}

fun PaidPublicationTask.recordEntitlementPlanCreated(entitlementPlanId: String) {
    check(entitlementPlanId.isNotBlank()) {
        "Entitlement plan id must not be blank."
    }

    this.entitlementPlanId?.let {
        check(it == entitlementPlanId) {
            "Cannot record a different entitlement plan id."
        }
        return
    }

    this.entitlementPlanId = entitlementPlanId
    entitlementPlanStatus = EntitlementPlanStatus.CREATED
}

fun PaidPublicationTask.recordEntitlementPlanCancelled() {
    when (entitlementPlanStatus) {
        EntitlementPlanStatus.NONE,
        EntitlementPlanStatus.CANCELLED -> return

        EntitlementPlanStatus.ACTIVATED -> error("Cannot automatically cancel an activated entitlement plan.")

        EntitlementPlanStatus.CREATED -> entitlementPlanStatus = EntitlementPlanStatus.CANCELLED
    }
}

fun PaidPublicationTask.recordEntitlementPlanActivated() {
    when (entitlementPlanStatus) {
        EntitlementPlanStatus.ACTIVATED -> return
        EntitlementPlanStatus.CREATED -> entitlementPlanStatus = EntitlementPlanStatus.ACTIVATED
        EntitlementPlanStatus.NONE,
        EntitlementPlanStatus.CANCELLED -> error("Cannot activate an entitlement plan that has not been created.")
    }
}

fun PaidPublicationTask.markPublished(publishedAt: LocalDateTime) {
    when (paidPublicationStatus) {
        PaidPublicationStatus.PUBLISHED -> return
        PaidPublicationStatus.RUNNING -> {
            paidPublicationStatus = PaidPublicationStatus.PUBLISHED
            this.publishedAt = publishedAt
        }
        PaidPublicationStatus.PENDING -> error("Cannot publish a pending paid publication task.")
        PaidPublicationStatus.FAILED -> error("Cannot publish a failed paid publication task.")
        PaidPublicationStatus.REQUIRES_OPERATOR_REPAIR -> error("Cannot publish a paid publication task that requires operator repair.")
    }
}

fun PaidPublicationTask.markFailed(reason: String, failedAt: LocalDateTime) {
    check(reason.isNotBlank()) {
        "Paid publication failure reason must not be blank."
    }

    paidPublicationStatus = PaidPublicationStatus.FAILED
    this.failedAt = failedAt
    failedReason = reason
}

fun PaidPublicationTask.markRequiresOperatorRepair(reason: String, failedAt: LocalDateTime) {
    check(reason.isNotBlank()) {
        "Paid publication repair reason must not be blank."
    }

    paidPublicationStatus = PaidPublicationStatus.REQUIRES_OPERATOR_REPAIR
    this.failedAt = failedAt
    failedReason = reason
}
