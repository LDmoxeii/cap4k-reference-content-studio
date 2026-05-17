package com.only4.cap4k.reference.contentstudio.application.sagas.paid.publication

import com.only4.cap4k.ddd.core.application.saga.SagaHandler
import com.only4.cap4k.ddd.core.application.saga.SagaParam
import com.only4.cap4k.ddd.core.share.annotation.Retry
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.ActivateAccessEntitlementPlanCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.CancelEntitlementPlanIfCreatedCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.CreateAccessEntitlementPlanCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.MarkPaidPublicationFailedCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.PublishPaidPublicationContentCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.ReleasePayoutHoldIfReservedCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.ReserveCreatorPayoutHoldCmd
import java.util.UUID
import org.springframework.stereotype.Service

object PaidPublicationSaga {

    const val PROCESS_RESERVE_PAYOUT_HOLD = "reserve-payout-hold"
    const val PROCESS_CREATE_ENTITLEMENT_PLAN = "create-entitlement-plan"
    const val PROCESS_PUBLISH_CONTENT = "publish-content"
    const val PROCESS_ACTIVATE_ENTITLEMENT_PLAN = "activate-entitlement-plan"
    const val PROCESS_CANCEL_ENTITLEMENT_PLAN = "cancel-entitlement-plan-if-created"
    const val PROCESS_RELEASE_PAYOUT_HOLD = "release-payout-hold-if-reserved"
    const val PROCESS_MARK_PUBLICATION_FAILED = "mark-paid-publication-failed"

    @Service
    class Handler : SagaHandler<Request, Response> {

        override fun exec(request: Request): Response =
            try {
                runForward(request)
                Response(published = true)
            } catch (primary: Throwable) {
                compensateBestEffort(request, primary)
                throw primary
            }

        private fun runForward(request: Request) {
            execProcess(
                PROCESS_RESERVE_PAYOUT_HOLD,
                ReserveCreatorPayoutHoldCmd.Request(request.paidPublicationTaskId),
            )
            execProcess(
                PROCESS_CREATE_ENTITLEMENT_PLAN,
                CreateAccessEntitlementPlanCmd.Request(request.paidPublicationTaskId),
            )
            execProcess(
                PROCESS_PUBLISH_CONTENT,
                PublishPaidPublicationContentCmd.Request(request.paidPublicationTaskId),
            )
            execProcess(
                PROCESS_ACTIVATE_ENTITLEMENT_PLAN,
                ActivateAccessEntitlementPlanCmd.Request(request.paidPublicationTaskId),
            )
        }

        private fun compensateBestEffort(request: Request, primary: Throwable) {
            val compensationFailures = mutableListOf<Throwable>()
            val reason = primary.message ?: "Paid publication saga failed."

            runCompensation(compensationFailures) {
                execProcess(
                    PROCESS_CANCEL_ENTITLEMENT_PLAN,
                    CancelEntitlementPlanIfCreatedCmd.Request(
                        paidPublicationTaskId = request.paidPublicationTaskId,
                        reason = reason,
                    ),
                )
            }
            runCompensation(compensationFailures) {
                execProcess(
                    PROCESS_RELEASE_PAYOUT_HOLD,
                    ReleasePayoutHoldIfReservedCmd.Request(
                        paidPublicationTaskId = request.paidPublicationTaskId,
                        reason = reason,
                    ),
                )
            }
            runCompensation(compensationFailures) {
                execProcess(
                    PROCESS_MARK_PUBLICATION_FAILED,
                    MarkPaidPublicationFailedCmd.Request(
                        paidPublicationTaskId = request.paidPublicationTaskId,
                        failedReason = buildFailureReason(primary, compensationFailures),
                    ),
                )
            }

            compensationFailures.forEach(primary::addSuppressed)
        }

        private fun runCompensation(compensationFailures: MutableList<Throwable>, block: () -> Unit) {
            try {
                block()
            } catch (failure: Throwable) {
                compensationFailures += failure
            }
        }

        private fun buildFailureReason(primary: Throwable, compensationFailures: List<Throwable>): String {
            val primaryMessage = primary.message ?: primary.javaClass.simpleName
            if (compensationFailures.isEmpty()) {
                return "Paid publication saga failed: $primaryMessage"
            }

            val compensationMessages = compensationFailures.joinToString("; ") { failure ->
                failure.message ?: failure.javaClass.simpleName
            }
            return "Paid publication saga failed: $primaryMessage. Compensation failures: $compensationMessages"
        }
    }

    @Retry(
        retryTimes = 30,
        retryIntervals = [1, 1, 5, 5, 10],
        expireAfter = 1440,
    )
    data class Request(
        val paidPublicationTaskId: UUID
    ) : SagaParam<Response>

    data class Response(
        val published: Boolean = true
    )
}
