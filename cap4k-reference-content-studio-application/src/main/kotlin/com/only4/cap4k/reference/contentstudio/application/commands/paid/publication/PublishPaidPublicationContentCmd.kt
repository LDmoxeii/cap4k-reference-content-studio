package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.PublishContentCmd
import com.only4.cap4k.reference.contentstudio.domain._share.meta.paid_publication_task.SPaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.markPublished
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object PublishPaidPublicationContentCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val task = loadTask(request.paidPublicationTaskId)
            if (task.paidPublicationStatus == PaidPublicationStatus.PUBLISHED) {
                return Response(published = false)
            }
            check(task.paidPublicationStatus == PaidPublicationStatus.RUNNING) {
                "Paid publication task ${task.id} is not running."
            }
            check(task.payoutHoldStatus == PayoutHoldStatus.RESERVED) {
                "Paid publication task ${task.id} has no reserved payout hold."
            }
            check(task.entitlementPlanStatus == EntitlementPlanStatus.CREATED) {
                "Paid publication task ${task.id} has no created entitlement plan."
            }

            val now = LocalDateTime.now()
            val response =
                // Local synchronous reuse of the existing content write boundary; process orchestration remains in Saga.
                Mediator.cmd.send(
                    PublishContentCmd.Request(
                        contentId = task.contentId,
                        publishedAt = now,
                        releaseReadinessSatisfied = true,
                    )
                )
            if (response.published) {
                loadTask(request.paidPublicationTaskId).markPublished(now)
                Mediator.uow.save()
            }

            return Response(published = response.published)
        }
    }

    data class Request(
        val paidPublicationTaskId: UUID
    ) : RequestParam<Response>

    data class Response(
        val published: Boolean
    )

    private fun loadTask(paidPublicationTaskId: UUID): PaidPublicationTask =
        checkNotNull(Mediator.repositories.findOne(SPaidPublicationTask.predicateById(paidPublicationTaskId))) {
            "Paid publication task $paidPublicationTaskId was not found."
        }
}
