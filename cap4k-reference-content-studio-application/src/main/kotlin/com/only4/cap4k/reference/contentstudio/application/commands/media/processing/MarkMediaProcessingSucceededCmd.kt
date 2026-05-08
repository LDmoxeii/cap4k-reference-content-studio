package com.only4.cap4k.reference.contentstudio.application.commands.media.processing

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingTaskRepository
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.markSucceeded
import java.util.UUID
import org.springframework.stereotype.Service

object MarkMediaProcessingSucceededCmd {

    @Service
    class Handler(
        private val mediaProcessingTaskRepository: MediaProcessingTaskRepository,
    ) : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val task = checkNotNull(mediaProcessingTaskRepository.findByContentId(request.contentId)) {
                "Media processing task for content ${request.contentId} was not found."
            }
            check(request.externalTaskId == null || task.externalTaskId == request.externalTaskId) {
                "External task id mismatch for content ${request.contentId}."
            }
            task.markSucceeded()
            mediaProcessingTaskRepository.save(task)

            return Response
        }
    }

    data class Request(
        val contentId: UUID,
        val externalTaskId: String?
    ) : RequestParam<Response>

    data object Response

}
