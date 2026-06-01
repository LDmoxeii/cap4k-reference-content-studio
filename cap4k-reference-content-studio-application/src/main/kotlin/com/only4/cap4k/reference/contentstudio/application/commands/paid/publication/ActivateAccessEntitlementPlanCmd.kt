package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.ActivateAccessEntitlementPlanCli
import com.only4.cap4k.reference.contentstudio.domain._share.meta.paid_publication_task.SPaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.recordEntitlementPlanActivated
import java.time.LocalDateTime
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import org.springframework.stereotype.Service
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

@BuildingBlock(
    tag = "command",
    name = "ActivateAccessEntitlementPlan",
    packageName = "paid.publication",
    description = "activate access entitlement plan",
    aggregates = ["PaidPublicationTask"],
    family = "command"
)
object ActivateAccessEntitlementPlanCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val task = loadTask(request.paidPublicationTaskId)
            validateLoadedTaskForActivation(task)?.let {
                return it
            }

            val response =
                Mediator.requests.send(
                    ActivateAccessEntitlementPlanCli.Request(
                        paidPublicationTaskId = task.id,
                    )
                )
            if (!response.activated) {
                return Response(activated = false)
            }
            task.recordEntitlementPlanActivated(LocalDateTime.now())
            Mediator.uow.save()

            return Response(activated = true)
        }
    }

    data class Request(
        val paidPublicationTaskId: PaidPublicationTaskId
    ) : RequestParam<Response>

    data class Response(
        val activated: Boolean
    )

    internal fun validateLoadedTaskForActivation(task: PaidPublicationTask): Response? {
        check(task.paidPublicationStatus == PaidPublicationStatus.PUBLISHED) {
            "Paid publication task ${task.id} is not published."
        }
        if (task.entitlementPlanStatus == EntitlementPlanStatus.ACTIVATED) {
            check(task.completedAt != null) {
                "Paid publication task ${task.id} has activated entitlement plan without completed time."
            }
            return Response(activated = false)
        }
        check(task.entitlementPlanStatus == EntitlementPlanStatus.CREATED) {
            "Paid publication task ${task.id} has no created entitlement plan."
        }
        return null
    }

    private fun loadTask(paidPublicationTaskId: PaidPublicationTaskId): PaidPublicationTask =
        checkNotNull(Mediator.repositories.findOne(SPaidPublicationTask.predicateById(paidPublicationTaskId), persist = true)) {
            "Paid publication task $paidPublicationTaskId was not found."
        }
}
