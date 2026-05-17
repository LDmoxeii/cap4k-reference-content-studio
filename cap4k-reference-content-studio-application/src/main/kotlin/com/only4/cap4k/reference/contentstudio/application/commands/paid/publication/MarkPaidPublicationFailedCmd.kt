package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.domain._share.meta.paid_publication_task.SPaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.markFailed
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.markRequiresOperatorRepair
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object MarkPaidPublicationFailedCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val task = loadTask(request.paidPublicationTaskId)
            if (task.paidPublicationStatus == PaidPublicationStatus.REQUIRES_OPERATOR_REPAIR) {
                return Response
            }

            val now = LocalDateTime.now()
            if (task.paidPublicationStatus == PaidPublicationStatus.PUBLISHED) {
                task.markRequiresOperatorRepair(request.failedReason, now)
            } else {
                task.markFailed(request.failedReason, now)
            }
            Mediator.uow.save()

            return Response
        }
    }

    data class Request(
        val paidPublicationTaskId: UUID,
        val failedReason: String
    ) : RequestParam<Response>

    data object Response

    private fun loadTask(paidPublicationTaskId: UUID): PaidPublicationTask =
        checkNotNull(Mediator.repositories.findOne(SPaidPublicationTask.predicateById(paidPublicationTaskId))) {
            "Paid publication task $paidPublicationTaskId was not found."
        }
}
