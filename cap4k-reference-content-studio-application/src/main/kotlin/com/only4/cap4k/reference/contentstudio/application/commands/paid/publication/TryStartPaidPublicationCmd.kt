package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.sagas.paid.publication.PaidPublicationSaga
import com.only4.cap4k.reference.contentstudio.domain._share.meta.content.SContent
import com.only4.cap4k.reference.contentstudio.domain._share.meta.media_processing_task.SMediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain._share.meta.paid_publication_task.SPaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.factory.PaidPublicationTaskFactory
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.recordSagaStarted
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object TryStartPaidPublicationCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val content = checkNotNull(Mediator.repositories.findOne(SContent.predicateById(request.contentId))) {
                "Content ${request.contentId} was not found."
            }
            val existingTask =
                Mediator.repositories.findFirst(
                    SPaidPublicationTask.predicate { schema ->
                        schema.contentId.eq(request.contentId)
                    }
                )
            if (existingTask?.publicationSagaId != null) {
                return Response(taskId = existingTask.id, started = false)
            }

            check(content.releasePolicy == ReleasePolicy.PAID) {
                "Content ${request.contentId} is not paid content."
            }
            check(content.contentStatus != ContentStatus.PUBLISHED) {
                "Content ${request.contentId} is already published."
            }
            val mediaProcessingTask =
                checkNotNull(
                    Mediator.repositories.findFirst(
                        SMediaProcessingTask.predicate { schema ->
                            schema.contentId.eq(request.contentId)
                        }
                    )
                ) {
                    "Media processing task for content ${request.contentId} was not found."
                }
            check(mediaProcessingTask.processingStatus == MediaProcessingStatus.SUCCEEDED) {
                "Media processing task for content ${request.contentId} has not succeeded."
            }

            val now = LocalDateTime.now()
            val task =
                existingTask ?: Mediator.factories.create(
                    PaidPublicationTaskFactory.Payload(
                        id = UUID.randomUUID(),
                        contentId = request.contentId,
                        now = now,
                    )
                )
            val sagaId =
                Mediator.requests.async(
                    PaidPublicationSaga.Request(
                        paidPublicationTaskId = task.id,
                    )
                )
            task.recordSagaStarted(sagaId, now)
            Mediator.uow.save()

            return Response(taskId = task.id, started = true)
        }
    }

    data class Request(
        val contentId: UUID
    ) : RequestParam<Response>

    data class Response(
        val taskId: UUID?,
        val started: Boolean
    )

}
