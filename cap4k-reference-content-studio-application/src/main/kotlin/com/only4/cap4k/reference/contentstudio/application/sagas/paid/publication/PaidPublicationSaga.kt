package com.only4.cap4k.reference.contentstudio.application.sagas.paid.publication

import com.only4.cap4k.ddd.core.application.saga.SagaHandler
import com.only4.cap4k.ddd.core.application.saga.SagaParam
import com.only4.cap4k.ddd.core.share.annotation.Retry
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.ActivateAccessEntitlementPlanCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.CancelEntitlementPlanIfCreatedCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.CreateAccessEntitlementPlanCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.MarkPaidPublicationContentPublishedCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.MarkPaidPublicationFailedCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.PublishPaidPublicationContentCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.ReleasePayoutHoldIfReservedCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.ReserveCreatorPayoutHoldCmd
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import org.springframework.stereotype.Service
import kotlin.intArrayOf
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

@BuildingBlock(
    tag = "saga",
    name = "PaidPublicationSaga",
    packageName = "paid.publication",
    description = "paid publication saga",
    aggregates = ["PaidPublicationTask"],
    family = "saga"
)
object PaidPublicationSaga {

    const val PROCESS_RESERVE_PAYOUT_HOLD = "reserve-payout-hold"
    const val PROCESS_CREATE_ENTITLEMENT_PLAN = "create-entitlement-plan"
    const val PROCESS_PUBLISH_CONTENT = "publish-content"
    const val PROCESS_MARK_CONTENT_PUBLISHED = "mark-content-published"
    const val PROCESS_ACTIVATE_ENTITLEMENT_PLAN = "activate-entitlement-plan"
    const val PROCESS_CANCEL_ENTITLEMENT_PLAN = "cancel-entitlement-plan-if-created"
    const val PROCESS_RELEASE_PAYOUT_HOLD = "release-payout-hold-if-reserved"
    const val PROCESS_MARK_PUBLICATION_FAILED = "mark-paid-publication-failed"

    @Service
    class Handler : SagaHandler<Request, Response> {

        override fun exec(request: Request): Response {
            runForward(request)
            return Response(published = true)
        }

        private fun runForward(request: Request) {
            execCompensableProcess(
                PROCESS_RESERVE_PAYOUT_HOLD,
                ReserveCreatorPayoutHoldCmd.Request(request.paidPublicationTaskId),
                PROCESS_RELEASE_PAYOUT_HOLD,
            ) {
                ReleasePayoutHoldIfReservedCmd.Request(
                    paidPublicationTaskId = request.paidPublicationTaskId,
                    reason = "Paid publication saga compensation requested after payout hold reservation.",
                )
            }
            execCompensableProcess(
                PROCESS_CREATE_ENTITLEMENT_PLAN,
                CreateAccessEntitlementPlanCmd.Request(request.paidPublicationTaskId),
                PROCESS_CANCEL_ENTITLEMENT_PLAN,
            ) {
                CancelEntitlementPlanIfCreatedCmd.Request(
                    paidPublicationTaskId = request.paidPublicationTaskId,
                    reason = "Paid publication saga compensation requested after entitlement plan creation.",
                )
            }
            val publish = execProcess(
                PROCESS_PUBLISH_CONTENT,
                PublishPaidPublicationContentCmd.Request(request.paidPublicationTaskId),
            )
            if (!publish.published) {
                requestCompensation(
                    code = "PAID_PUBLICATION_REJECTED",
                    reason = "Paid publication content was not published.",
                )
            }
            execCompensableProcess(
                PROCESS_MARK_CONTENT_PUBLISHED,
                MarkPaidPublicationContentPublishedCmd.Request(request.paidPublicationTaskId),
                PROCESS_MARK_PUBLICATION_FAILED,
            ) {
                MarkPaidPublicationFailedCmd.Request(
                    paidPublicationTaskId = request.paidPublicationTaskId,
                    failedReason = "Paid publication saga compensation requested after content publication was marked.",
                )
            }
            val activation = execProcess(
                PROCESS_ACTIVATE_ENTITLEMENT_PLAN,
                ActivateAccessEntitlementPlanCmd.Request(request.paidPublicationTaskId),
            )
            if (!activation.activated) {
                requestCompensation(
                    code = "ENTITLEMENT_ACTIVATION_REJECTED",
                    reason = "Entitlement plan was not activated.",
                )
            }
        }
    }

    @Retry(
        retryTimes = 1,
        retryIntervals = [1],
        expireAfter = 1440,
    )
    data class Request(
        val paidPublicationTaskId: PaidPublicationTaskId
    ) : SagaParam<Response>

    data class Response(
        val published: Boolean = true
    )
}
