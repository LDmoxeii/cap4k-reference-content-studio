package com.only4.cap4k.reference.contentstudio.application.commands.media.processing

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingTaskRepository
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.markSucceeded
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

object MarkMediaProcessingSucceededCmd {

    @Service
    @Transactional
    open class Handler(
        private val mediaProcessingTaskRepository: MediaProcessingTaskRepository,
    ) : Command<Request, Response> {

        open override fun exec(request: Request): Response {
            check(request.externalTaskId.isNotBlank()) {
                "External task id must not be blank."
            }
            val task = checkNotNull(mediaProcessingTaskRepository.findByExternalTaskId(request.externalTaskId)) {
                "Media processing task for external task id ${request.externalTaskId} was not found."
            }
            task.markSucceeded()
            mediaProcessingTaskRepository.save(task)

            return Response
        }
    }

    data class Request(
        val externalTaskId: String
    ) : RequestParam<Response>

    data object Response

}
