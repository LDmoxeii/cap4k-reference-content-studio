package com.only4.cap4k.reference.contentstudio.application.commands.content.workflow

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.submitForReview
import com.only4.cap4k.reference.contentstudio.domain._share.meta.content.SContent
import java.util.UUID
import org.springframework.stereotype.Service

object SubmitContentForReviewCmd {

    @Service
    open class Handler : Command<Request, Response> {

        open override fun exec(request: Request): Response {
            val content = checkNotNull(Mediator.repositories.findOne(SContent.predicateById(request.contentId), persist = true)) {
                "Content ${request.contentId} was not found."
            }
            content.submitForReview()
            Mediator.uow.save()

            return Response
        }
    }

    data class Request(
        val contentId: UUID
    ) : RequestParam<Response>

    data object Response

}
