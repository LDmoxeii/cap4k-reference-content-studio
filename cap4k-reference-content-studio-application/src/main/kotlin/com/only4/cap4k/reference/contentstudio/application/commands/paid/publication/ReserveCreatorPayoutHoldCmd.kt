package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.ReserveCreatorPayoutHoldCli
import com.only4.cap4k.reference.contentstudio.domain._share.meta.paid_publication_task.SPaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.recordPayoutHoldReserved
import java.util.UUID
import org.springframework.stereotype.Service

object ReserveCreatorPayoutHoldCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val task = loadTask(request.paidPublicationTaskId)
            if (task.payoutHoldStatus == PayoutHoldStatus.RESERVED || task.payoutHoldStatus == PayoutHoldStatus.RELEASED) {
                return Response(reserved = false)
            }
            if (task.paidPublicationStatus != PaidPublicationStatus.RUNNING || task.publicationSagaId == null) {
                return Response(reserved = false)
            }

            val response =
                Mediator.requests.send(
                    ReserveCreatorPayoutHoldCli.Request(
                        paidPublicationTaskId = task.id,
                    )
                )
            task.recordPayoutHoldReserved(response.payoutHoldId)
            Mediator.uow.save()

            return Response(reserved = true)
        }
    }

    data class Request(
        val paidPublicationTaskId: UUID
    ) : RequestParam<Response>

    data class Response(
        val reserved: Boolean
    )

    private fun loadTask(paidPublicationTaskId: UUID): PaidPublicationTask =
        checkNotNull(Mediator.repositories.findOne(SPaidPublicationTask.predicateById(paidPublicationTaskId))) {
            "Paid publication task $paidPublicationTaskId was not found."
        }
}
