package com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task

import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus
import java.time.LocalDateTime

fun PaidPublicationTask.recordSagaStarted(sagaId: String, now: LocalDateTime) {
    check(sagaId.isNotBlank()) {
        "Publication saga id must not be blank."
    }

    when (paidPublicationStatus) {
        PaidPublicationStatus.PENDING -> {
            check(publicationSagaId == null) {
                "Cannot start a pending paid publication task that already has a saga id."
            }
            publicationSagaId = sagaId
            paidPublicationStatus = PaidPublicationStatus.RUNNING
            startedAt = now
        }

        PaidPublicationStatus.RUNNING -> {
            check(publicationSagaId == sagaId) {
                "Cannot record a different publication saga id."
            }
        }

        PaidPublicationStatus.PUBLISHED,
        PaidPublicationStatus.FAILED,
        PaidPublicationStatus.REQUIRES_OPERATOR_REPAIR -> error("Cannot start a paid publication task that is not pending.")
    }
}

fun PaidPublicationTask.recordPayoutHoldReserved(payoutHoldId: String) {
    check(payoutHoldId.isNotBlank()) {
        "Payout hold id must not be blank."
    }
    check(paidPublicationStatus == PaidPublicationStatus.RUNNING) {
        "Cannot reserve payout hold for a paid publication task that is not running."
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
    check(paidPublicationStatus == PaidPublicationStatus.RUNNING) {
        "Cannot create entitlement plan for a paid publication task that is not running."
    }
    check(payoutHoldStatus == PayoutHoldStatus.RESERVED) {
        "Cannot create entitlement plan before payout hold is reserved."
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

fun PaidPublicationTask.recordEntitlementPlanActivated(completedAt: LocalDateTime) {
    check(paidPublicationStatus == PaidPublicationStatus.PUBLISHED) {
        "Cannot activate entitlement plan before paid publication is published."
    }

    when (entitlementPlanStatus) {
        EntitlementPlanStatus.ACTIVATED -> {
            check(this.completedAt != null) {
                "Cannot treat entitlement activation as no-op before completed time is recorded."
            }
            return
        }

        EntitlementPlanStatus.CREATED -> {
            entitlementPlanStatus = EntitlementPlanStatus.ACTIVATED
            this.completedAt = completedAt
        }

        EntitlementPlanStatus.NONE,
        EntitlementPlanStatus.CANCELLED -> error("Cannot activate an entitlement plan that has not been created.")
    }
}

fun PaidPublicationTask.markPublished(publishedAt: LocalDateTime) {
    when (paidPublicationStatus) {
        PaidPublicationStatus.PUBLISHED -> {
            check(payoutHoldStatus == PayoutHoldStatus.RESERVED) {
                "Cannot treat published duplicate as no-op before payout hold is reserved."
            }
            check(entitlementPlanStatus == EntitlementPlanStatus.CREATED) {
                "Cannot treat published duplicate as no-op before entitlement plan is created."
            }
            check(this.publishedAt != null) {
                "Cannot treat published duplicate as no-op before published time is recorded."
            }
            return
        }

        PaidPublicationStatus.RUNNING -> {
            check(payoutHoldStatus == PayoutHoldStatus.RESERVED) {
                "Cannot publish paid publication before payout hold is reserved."
            }
            check(entitlementPlanStatus == EntitlementPlanStatus.CREATED) {
                "Cannot publish paid publication before entitlement plan is created."
            }
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
    check(paidPublicationStatus != PaidPublicationStatus.PUBLISHED) {
        "Published paid publication tasks require operator repair instead of safe failure."
    }

    if (paidPublicationStatus == PaidPublicationStatus.FAILED) {
        check(failedReason == reason) {
            "Cannot replace paid publication failure reason."
        }
        return
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
