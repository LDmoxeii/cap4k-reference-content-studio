package com.only4.cap4k.reference.contentstudio.application.commands.content.workflow

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.approve
import com.only4.cap4k.reference.contentstudio.domain._share.meta.content.SContent
import java.time.LocalDateTime
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import org.springframework.stereotype.Service

object ApproveContentReviewCmd {

    @Service
    open class Handler : Command<Request, Response> {

        open override fun exec(request: Request): Response {
            val content = checkNotNull(Mediator.repositories.findOne(SContent.predicateById(request.contentId), persist = true)) {
                "Content ${request.contentId} was not found."
            }
            content.approve(request.reviewerId, request.reviewedAt)
            Mediator.uow.save()

            return Response
        }
    }

    data class Request(
        val contentId: ContentId,
        val reviewerId: ReviewerId,
        val reviewedAt: LocalDateTime
    ) : RequestParam<Response>

    data object Response

}
