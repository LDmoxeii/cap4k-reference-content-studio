package com.only4.cap4k.reference.contentstudio.application.commands.content.workflow

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.ports.ContentRepository
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.submitForReview
import java.util.UUID
import org.springframework.stereotype.Service

object SubmitContentForReviewCmd {

    @Service
    class Handler(
        private val contentRepository: ContentRepository,
    ) : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val content = checkNotNull(contentRepository.findById(request.contentId)) {
                "Content ${request.contentId} was not found."
            }
            content.submitForReview()
            contentRepository.save(content)

            return Response
        }
    }

    data class Request(
        val contentId: UUID
    ) : RequestParam<Response>

    data object Response

}
