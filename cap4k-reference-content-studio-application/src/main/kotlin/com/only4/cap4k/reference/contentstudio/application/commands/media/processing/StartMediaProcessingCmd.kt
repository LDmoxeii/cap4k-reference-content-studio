package com.only4.cap4k.reference.contentstudio.application.commands.media.processing

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.media.processing.TriggerMediaProcessingCli
import com.only4.cap4k.reference.contentstudio.domain._share.meta.content.SContent
import com.only4.cap4k.reference.contentstudio.domain._share.meta.media_processing_task.SMediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.factory.MediaProcessingTaskFactory
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.markSubmitted
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object StartMediaProcessingCmd {

    @Service
    open class Handler : Command<Request, Response> {

        open override fun exec(request: Request): Response {
            val content = checkNotNull(Mediator.repositories.findOne(SContent.predicateById(request.contentId))) {
                "Content ${request.contentId} was not found."
            }
            val task =
                Mediator.repositories.findFirst(
                    SMediaProcessingTask.predicate { schema ->
                        schema.contentId.eq(request.contentId)
                    }
                ) ?: Mediator.factories.create(
                    MediaProcessingTaskFactory.Payload(
                        id = UUID.randomUUID(),
                        contentId = request.contentId,
                        externalTaskId = null,
                        processingStatus = MediaProcessingStatus.PENDING,
                        dbCreatedAt = LocalDateTime.now(),
                        dbUpdatedAt = LocalDateTime.now(),
                    )
                )
            if (task.processingStatus == MediaProcessingStatus.SUCCEEDED) {
                return Response
            }
            val response =
                Mediator.requests.send(
                    TriggerMediaProcessingCli.Request(
                        contentId = content.id,
                        mediaSourceKey = content.mediaSourceKey,
                    )
                )
            check(response.accepted) {
                "Media processing was not accepted for content ${request.contentId}."
            }
            val externalTaskId = requireNotNull(response.externalTaskId) {
                "Media processing must return an external task id for content ${request.contentId}."
            }
            task.markSubmitted(externalTaskId)
            Mediator.uow.save()

            return Response
        }
    }

    data class Request(
        val contentId: UUID
    ) : RequestParam<Response>

    data object Response

}
