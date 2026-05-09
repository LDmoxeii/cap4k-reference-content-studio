package com.only4.cap4k.reference.contentstudio.application.commands.content.workflow

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.ports.ContentRepository
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.approve
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

object ApproveContentReviewCmd {

    @Service
    @Transactional
    open class Handler(
        private val contentRepository: ContentRepository,
    ) : Command<Request, Response> {

        open override fun exec(request: Request): Response {
            val content = checkNotNull(contentRepository.findById(request.contentId)) {
                "Content ${request.contentId} was not found."
            }
            content.approve(request.reviewerId, request.reviewedAt)
            contentRepository.save(content)

            return Response
        }
    }

    data class Request(
        val contentId: UUID,
        val reviewerId: UUID,
        val reviewedAt: LocalDateTime
    ) : RequestParam<Response>

    data object Response

}
