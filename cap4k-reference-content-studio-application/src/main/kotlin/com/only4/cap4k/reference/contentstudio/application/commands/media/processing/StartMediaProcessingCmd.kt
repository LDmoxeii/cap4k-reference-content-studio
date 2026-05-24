package com.only4.cap4k.reference.contentstudio.application.commands.media.processing

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.media.processing.TriggerMediaProcessingCli
import com.only4.cap4k.reference.contentstudio.domain._share.meta.media_processing_task.SMediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.factory.MediaProcessingTaskFactory
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.markSubmitted
import java.time.LocalDateTime
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import org.springframework.stereotype.Service

object StartMediaProcessingCmd {

    @Service
    open class Handler : Command<Request, Response> {

        open override fun exec(request: Request): Response {
            check(request.mediaSourceKey.isNotBlank()) {
                "Media source key must not be blank."
            }
            val task =
                Mediator.repositories.findFirst(
                    SMediaProcessingTask.predicate { schema ->
                        schema.contentId.eq(request.contentId)
                    },
                    persist = true
                ) ?: Mediator.factories.create(
                    MediaProcessingTaskFactory.Payload(
                        id = MediaProcessingTaskId.new(),
                        contentId = request.contentId,
                        externalTaskId = null,
                        processingStatus = MediaProcessingStatus.PENDING,
                        dbCreatedAt = LocalDateTime.now(),
                        dbUpdatedAt = LocalDateTime.now(),
                    )
                )
            if (
                task.processingStatus == MediaProcessingStatus.SUBMITTED ||
                task.processingStatus == MediaProcessingStatus.SUCCEEDED
            ) {
                return Response
            }
            val response =
                Mediator.requests.send(
                    TriggerMediaProcessingCli.Request(
                        contentId = request.contentId,
                        mediaSourceKey = request.mediaSourceKey,
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
        val contentId: ContentId,
        val mediaSourceKey: String,
    ) : RequestParam<Response>

    data object Response

}
