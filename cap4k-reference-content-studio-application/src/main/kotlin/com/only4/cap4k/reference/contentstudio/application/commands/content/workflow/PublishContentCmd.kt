package com.only4.cap4k.reference.contentstudio.application.commands.content.workflow

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.ports.ContentRepository
import com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingTaskRepository
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.publish
import com.only4.cap4k.reference.contentstudio.domain.services.PublicationEligibilityDomainService
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object PublishContentCmd {

    @Service
    class Handler(
        private val contentRepository: ContentRepository,
        private val mediaProcessingTaskRepository: MediaProcessingTaskRepository,
        private val publicationEligibilityDomainService: PublicationEligibilityDomainService,
    ) : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val content = checkNotNull(contentRepository.findById(request.contentId)) {
                "Content ${request.contentId} was not found."
            }
            val mediaProcessingTask = checkNotNull(mediaProcessingTaskRepository.findByContentId(request.contentId)) {
                "Media processing task for content ${request.contentId} was not found."
            }
            check(publicationEligibilityDomainService.evaluate(content, mediaProcessingTask)) {
                "Content ${request.contentId} is not eligible for publication."
            }
            content.publish(request.publishedAt)
            contentRepository.save(content)

            return Response
        }
    }

    data class Request(
        val contentId: UUID,
        val publishedAt: LocalDateTime
    ) : RequestParam<Response>

    data object Response

}
