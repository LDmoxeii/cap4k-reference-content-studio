package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.ActivateAccessEntitlementPlanCli
import com.only4.cap4k.reference.contentstudio.domain._share.meta.paid_publication_task.SPaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.recordEntitlementPlanActivated
import java.util.UUID
import org.springframework.stereotype.Service

object ActivateAccessEntitlementPlanCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val task = loadTask(request.paidPublicationTaskId)
            if (task.entitlementPlanStatus == EntitlementPlanStatus.ACTIVATED) {
                return Response(activated = false)
            }

            val response =
                Mediator.requests.send(
                    ActivateAccessEntitlementPlanCli.Request(
                        paidPublicationTaskId = task.id,
                    )
                )
            check(response.activated) {
                "Entitlement plan activation was not accepted for paid publication task ${task.id}."
            }
            task.recordEntitlementPlanActivated()
            Mediator.uow.save()

            return Response(activated = true)
        }
    }

    data class Request(
        val paidPublicationTaskId: UUID
    ) : RequestParam<Response>

    data class Response(
        val activated: Boolean
    )

    private fun loadTask(paidPublicationTaskId: UUID): PaidPublicationTask =
        checkNotNull(Mediator.repositories.findOne(SPaidPublicationTask.predicateById(paidPublicationTaskId))) {
            "Paid publication task $paidPublicationTaskId was not found."
        }
}
