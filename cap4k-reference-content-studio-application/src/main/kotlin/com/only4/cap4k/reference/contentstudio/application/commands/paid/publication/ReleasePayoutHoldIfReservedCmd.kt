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
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import org.springframework.stereotype.Service

object ReleasePayoutHoldIfReservedCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val task = loadTask(request.paidPublicationTaskId)
            validateLoadedTaskForRelease(task)?.let {
                return it
            }
            if (task.payoutHoldStatus == PayoutHoldStatus.CAPTURED) {
                val response = applyCapturedHoldRepair(task, LocalDateTime.now())
                Mediator.uow.save()
                return response
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

            return Response(released = true, decision = Decision.Released)
        }
    }

    data class Request(
        val paidPublicationTaskId: PaidPublicationTaskId,
        val reason: String
    ) : RequestParam<Response>

    data class Response(
        val released: Boolean,
        val decision: Decision,
    )

    enum class Decision {
        Released,
        NoPayoutHold,
        AlreadyReleased,
        CapturedRequiresOperatorRepair,
    }

    internal fun validateLoadedTaskForRelease(task: PaidPublicationTask): Response? =
        when (task.payoutHoldStatus) {
            PayoutHoldStatus.NONE -> Response(released = false, decision = Decision.NoPayoutHold)
            PayoutHoldStatus.RELEASED -> Response(released = false, decision = Decision.AlreadyReleased)
            PayoutHoldStatus.RESERVED,
            PayoutHoldStatus.CAPTURED -> null
        }

    internal fun applyCapturedHoldRepair(
        task: PaidPublicationTask,
        failedAt: LocalDateTime,
    ): Response {
        task.markRequiresOperatorRepair(
            "Payout hold is captured and cannot be released automatically.",
            failedAt,
        )
        return Response(released = false, decision = Decision.CapturedRequiresOperatorRepair)
    }

    private fun loadTask(paidPublicationTaskId: PaidPublicationTaskId): PaidPublicationTask =
        checkNotNull(Mediator.repositories.findOne(SPaidPublicationTask.predicateById(paidPublicationTaskId), persist = true)) {
            "Paid publication task $paidPublicationTaskId was not found."
        }
}
