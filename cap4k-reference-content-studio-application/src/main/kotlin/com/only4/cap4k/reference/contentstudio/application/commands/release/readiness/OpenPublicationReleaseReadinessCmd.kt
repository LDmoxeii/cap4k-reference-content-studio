package com.only4.cap4k.reference.contentstudio.application.commands.release.readiness

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.domain._share.meta.publication_release_readiness.SPublicationReleaseReadiness
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
            val readiness =
                Mediator.repositories.findFirst(
                    SPublicationReleaseReadiness.predicate { schema ->
                        schema.contentId.eq(request.contentId)
                    }
                ) ?: Mediator.factories.create(
                    PublicationReleaseReadinessFactory.Payload(
                        id = UUID.randomUUID(),
                        contentId = request.contentId,
                        mediaProcessingTaskId = request.mediaProcessingTaskId,
                        readinessState = PublicationReleaseReadinessState.WAITING,
                        copyrightStatus = CopyrightReviewStatus.WAITING,
                        manualConfirmationStatus = ManualReleaseConfirmationStatus.WAITING,
                        releaseWindowOpensAt = request.releaseWindowOpensAt,
                        releaseWindowClosesAt = request.releaseWindowClosesAt,
                        readyAt = null,
                        cancelReason = null,
                        dbCreatedAt = LocalDateTime.now(),
                        dbUpdatedAt = LocalDateTime.now(),
                    )
                )
            Mediator.uow.save()

            return Response(
                readinessId = readiness.id
            )
        }
    }

    data class Request(
        val contentId: UUID,
        val mediaProcessingTaskId: UUID,
        val releaseWindowOpensAt: LocalDateTime,
        val releaseWindowClosesAt: LocalDateTime
    ) : RequestParam<Response>

    data class Response(
        val readinessId: UUID
    )

}
