package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.domain._share.meta.content.SContent
import com.only4.cap4k.reference.contentstudio.domain._share.meta.paid_publication_task.SPaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.markFailed
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.markRequiresOperatorRepair
import java.time.LocalDateTime
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import org.springframework.stereotype.Service

object MarkPaidPublicationFailedCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val task = loadTask(request.paidPublicationTaskId)
            if (
                task.paidPublicationStatus == PaidPublicationStatus.FAILED ||
                task.paidPublicationStatus == PaidPublicationStatus.REQUIRES_OPERATOR_REPAIR
            ) {
                return Response
            }

            val now = LocalDateTime.now()
            if (task.paidPublicationStatus == PaidPublicationStatus.PUBLISHED || contentIsPublished(task)) {
                task.markRequiresOperatorRepair(request.failedReason, now)
            } else {
                task.markFailed(request.failedReason, now)
            }
            Mediator.uow.save()

            return Response
        }
    }

    data class Request(
        val paidPublicationTaskId: PaidPublicationTaskId,
        val failedReason: String
    ) : RequestParam<Response>

    data object Response

    private fun loadTask(paidPublicationTaskId: PaidPublicationTaskId): PaidPublicationTask =
        checkNotNull(Mediator.repositories.findOne(SPaidPublicationTask.predicateById(paidPublicationTaskId), persist = true)) {
            "Paid publication task $paidPublicationTaskId was not found."
        }

    private fun contentIsPublished(task: PaidPublicationTask): Boolean =
        checkNotNull(Mediator.repositories.findOne(SContent.predicateById(task.contentId))) {
            "Content ${task.contentId} was not found."
        }.contentStatus == ContentStatus.PUBLISHED
}
