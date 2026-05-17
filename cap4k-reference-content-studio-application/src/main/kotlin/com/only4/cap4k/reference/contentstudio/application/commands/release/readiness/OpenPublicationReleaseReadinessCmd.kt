package com.only4.cap4k.reference.contentstudio.application.commands.release.readiness

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.domain._share.meta.content.SContent
import com.only4.cap4k.reference.contentstudio.domain._share.meta.media_processing_task.SMediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain._share.meta.publication_release_readiness.SPublicationReleaseReadiness
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.CopyrightReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.ManualReleaseConfirmationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.PublicationReleaseReadinessState
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.factory.PublicationReleaseReadinessFactory
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object OpenPublicationReleaseReadinessCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val content = checkNotNull(Mediator.repositories.findOne(SContent.predicateById(request.contentId))) {
                "Content ${request.contentId} was not found."
            }
            if (content.releasePolicy != ReleasePolicy.GATED) {
                return Response(readinessId = null, opened = false)
            }

            val mediaProcessingTask =
                checkNotNull(Mediator.repositories.findOne(SMediaProcessingTask.predicateById(request.mediaProcessingTaskId))) {
                    "Media processing task ${request.mediaProcessingTaskId} was not found."
                }
            check(mediaProcessingTask.contentId == request.contentId) {
                "Media processing task ${request.mediaProcessingTaskId} does not belong to content ${request.contentId}."
            }
            check(mediaProcessingTask.processingStatus == MediaProcessingStatus.SUCCEEDED) {
                "Media processing task ${request.mediaProcessingTaskId} has not succeeded."
            }

            val releaseWindowOpensAt = requireNotNull(content.releaseWindowOpensAt) {
                "Gated content ${request.contentId} must have release window opensAt."
            }
            val releaseWindowClosesAt = requireNotNull(content.releaseWindowClosesAt) {
                "Gated content ${request.contentId} must have release window closesAt."
            }
            check(!releaseWindowClosesAt.isBefore(releaseWindowOpensAt)) {
                "Gated content ${request.contentId} release window closes before it opens."
            }

            val existing =
                Mediator.repositories.findFirst(
                    SPublicationReleaseReadiness.predicate { schema ->
                        schema.contentId.eq(request.contentId)
                    }
                )
            if (existing != null) {
                check(existing.mediaProcessingTaskId == request.mediaProcessingTaskId) {
                    "Publication release readiness for content ${request.contentId} belongs to a different media task."
                }
                return Response(readinessId = existing.id, opened = false)
            }

            val readiness = Mediator.factories.create(
                PublicationReleaseReadinessFactory.Payload(
                    id = UUID.randomUUID(),
                    contentId = request.contentId,
                    mediaProcessingTaskId = request.mediaProcessingTaskId,
                    readinessState = PublicationReleaseReadinessState.WAITING,
                    copyrightStatus = CopyrightReviewStatus.WAITING,
                    manualConfirmationStatus = ManualReleaseConfirmationStatus.WAITING,
                    releaseWindowOpensAt = releaseWindowOpensAt,
                    releaseWindowClosesAt = releaseWindowClosesAt,
                    releaseSagaId = null,
                    readyAt = null,
                    cancelReason = null,
                    dbCreatedAt = LocalDateTime.now(),
                    dbUpdatedAt = LocalDateTime.now(),
                )
            )
            Mediator.uow.save()

            return Response(
                readinessId = readiness.id,
                opened = true,
            )
        }
    }

    data class Request(
        val contentId: UUID,
        val mediaProcessingTaskId: UUID
    ) : RequestParam<Response>

    data class Response(
        val readinessId: UUID?,
        val opened: Boolean
    )

}
