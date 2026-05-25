package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.CancelAccessEntitlementPlanCli
import com.only4.cap4k.reference.contentstudio.domain._share.meta.paid_publication_task.SPaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.markRequiresOperatorRepair
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.recordEntitlementPlanCancelled
import java.time.LocalDateTime
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import org.springframework.stereotype.Service

object CancelEntitlementPlanIfCreatedCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val task = loadTask(request.paidPublicationTaskId)
            validateLoadedTaskForCancellation(task)?.let {
                return it
            }

            val now = LocalDateTime.now()
            if (task.entitlementPlanStatus == EntitlementPlanStatus.ACTIVATED) {
                val response = applyActivatedPlanRepair(task, now)
                Mediator.uow.save()
                return response
            }

            val response =
                Mediator.requests.send(
                    CancelAccessEntitlementPlanCli.Request(
                        paidPublicationTaskId = task.id,
                    )
                )
            check(response.cancelled) {
                "Entitlement plan cancellation was not accepted for paid publication task ${task.id}."
            }
            task.recordEntitlementPlanCancelled()
            Mediator.uow.save()

            return Response(cancelled = true, decision = Decision.Cancelled)
        }
    }

    data class Request(
        val paidPublicationTaskId: PaidPublicationTaskId,
        val reason: String
    ) : RequestParam<Response>

    data class Response(
        val cancelled: Boolean,
        val decision: Decision,
    )

    enum class Decision {
        Cancelled,
        NoEntitlementPlan,
        AlreadyCancelled,
        ActivatedRequiresOperatorRepair,
    }

    internal fun validateLoadedTaskForCancellation(task: PaidPublicationTask): Response? =
        when (task.entitlementPlanStatus) {
            EntitlementPlanStatus.NONE -> Response(cancelled = false, decision = Decision.NoEntitlementPlan)
            EntitlementPlanStatus.CANCELLED -> Response(cancelled = false, decision = Decision.AlreadyCancelled)
            EntitlementPlanStatus.CREATED,
            EntitlementPlanStatus.ACTIVATED -> null
        }

    internal fun applyActivatedPlanRepair(
        task: PaidPublicationTask,
        failedAt: LocalDateTime,
    ): Response {
        task.markRequiresOperatorRepair(
            "Entitlement plan is activated and cannot be cancelled automatically.",
            failedAt,
        )
        return Response(cancelled = false, decision = Decision.ActivatedRequiresOperatorRepair)
    }

    private fun loadTask(paidPublicationTaskId: PaidPublicationTaskId): PaidPublicationTask =
        checkNotNull(Mediator.repositories.findOne(SPaidPublicationTask.predicateById(paidPublicationTaskId), persist = true)) {
            "Paid publication task $paidPublicationTaskId was not found."
        }
}
