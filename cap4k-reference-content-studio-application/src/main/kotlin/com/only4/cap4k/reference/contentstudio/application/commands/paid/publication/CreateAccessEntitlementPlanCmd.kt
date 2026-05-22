package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.CreateAccessEntitlementPlanCli
import com.only4.cap4k.reference.contentstudio.domain._share.meta.paid_publication_task.SPaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.recordEntitlementPlanCreated
import java.util.UUID
import org.springframework.stereotype.Service

object CreateAccessEntitlementPlanCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val task = loadTask(request.paidPublicationTaskId)
            if (task.entitlementPlanStatus != EntitlementPlanStatus.NONE) {
                return Response(created = false)
            }
            check(task.paidPublicationStatus == PaidPublicationStatus.RUNNING) {
                "Paid publication task ${task.id} is not running."
            }
            check(task.payoutHoldStatus == PayoutHoldStatus.RESERVED) {
                "Paid publication task ${task.id} has no reserved payout hold."
            }

            val response =
                Mediator.requests.send(
                    CreateAccessEntitlementPlanCli.Request(
                        paidPublicationTaskId = task.id,
                    )
                )
            task.recordEntitlementPlanCreated(response.entitlementPlanId)
            Mediator.uow.save()

            return Response(created = true)
        }
    }

    data class Request(
        val paidPublicationTaskId: UUID
    ) : RequestParam<Response>

    data class Response(
        val created: Boolean
    )

    private fun loadTask(paidPublicationTaskId: UUID): PaidPublicationTask =
        checkNotNull(Mediator.repositories.findOne(SPaidPublicationTask.predicateById(paidPublicationTaskId), persist = true)) {
            "Paid publication task $paidPublicationTaskId was not found."
        }
}
