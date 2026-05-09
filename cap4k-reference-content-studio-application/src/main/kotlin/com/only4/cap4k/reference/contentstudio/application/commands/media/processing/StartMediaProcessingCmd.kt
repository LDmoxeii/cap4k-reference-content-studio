package com.only4.cap4k.reference.contentstudio.application.commands.media.processing

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.ports.ContentRepository
import com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingCli
import com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingTaskRepository
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.markSubmitted
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

object StartMediaProcessingCmd {

    @Service
    @Transactional
    open class Handler(
        private val contentRepository: ContentRepository,
        private val mediaProcessingTaskRepository: MediaProcessingTaskRepository,
        private val mediaProcessingCli: MediaProcessingCli,
    ) : Command<Request, Response> {

        open override fun exec(request: Request): Response {
            val content = checkNotNull(contentRepository.findById(request.contentId)) {
                "Content ${request.contentId} was not found."
            }
            val task =
                mediaProcessingTaskRepository.findByContentId(request.contentId)
                    ?: MediaProcessingTask(
                        id = UUID.randomUUID(),
                        contentId = request.contentId,
                        externalTaskId = null,
                        processingStatus = MediaProcessingStatus.PENDING.name,
                        dbCreatedAt = LocalDateTime.now(),
                        dbUpdatedAt = LocalDateTime.now(),
                    )
            val response = mediaProcessingCli.start(content.id, content.mediaSourceKey)
            check(response.accepted) {
                "Media processing was not accepted for content ${request.contentId}."
            }
            val externalTaskId = requireNotNull(response.externalTaskId) {
                "Media processing must return an external task id for content ${request.contentId}."
            }
            task.markSubmitted(externalTaskId)
            mediaProcessingTaskRepository.save(task)

            return Response
        }
    }

    data class Request(
        val contentId: UUID
    ) : RequestParam<Response>

    data object Response

}
