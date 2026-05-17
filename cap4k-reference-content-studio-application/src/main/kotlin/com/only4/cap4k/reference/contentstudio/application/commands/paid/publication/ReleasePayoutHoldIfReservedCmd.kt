package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.ReleaseCreatorPayoutHoldCli
import com.only4.cap4k.reference.contentstudio.domain._share.meta.paid_publication_task.SPaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.markRequiresOperatorRepair
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.recordPayoutHoldReleased
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object ReleasePayoutHoldIfReservedCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val task = loadTask(request.paidPublicationTaskId)
            if (task.payoutHoldStatus == PayoutHoldStatus.NONE || task.payoutHoldStatus == PayoutHoldStatus.RELEASED) {
                return Response(released = false)
            }
            if (task.payoutHoldStatus == PayoutHoldStatus.CAPTURED) {
                task.markRequiresOperatorRepair(
                    "Payout hold is captured and cannot be released automatically.",
                    LocalDateTime.now(),
                )
                Mediator.uow.save()
                return Response(released = false)
            }

            val response =
                Mediator.requests.send(
                    ReleaseCreatorPayoutHoldCli.Request(
                        paidPublicationTaskId = task.id,
                    )
                )
            check(response.released) {
                "Payout hold release was not accepted for paid publication task ${task.id}."
            }
            task.recordPayoutHoldReleased()
            Mediator.uow.save()

            return Response(released = true)
        }
    }

    data class Request(
        val paidPublicationTaskId: UUID,
        val reason: String
    ) : RequestParam<Response>

    data class Response(
        val released: Boolean
    )

    private fun loadTask(paidPublicationTaskId: UUID): PaidPublicationTask =
        checkNotNull(Mediator.repositories.findOne(SPaidPublicationTask.predicateById(paidPublicationTaskId))) {
            "Paid publication task $paidPublicationTaskId was not found."
        }
}
