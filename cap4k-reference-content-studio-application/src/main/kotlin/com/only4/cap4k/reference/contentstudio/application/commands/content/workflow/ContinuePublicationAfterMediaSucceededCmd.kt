package com.only4.cap4k.reference.contentstudio.application.commands.content.workflow

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.commands.release.readiness.OpenPublicationReleaseReadinessCmd
import com.only4.cap4k.reference.contentstudio.application.commands.release.readiness.RegisterPublicationReleaseSagaCmd
import com.only4.cap4k.reference.contentstudio.application.sagas.publication.PublicationReleaseSaga
import com.only4.cap4k.reference.contentstudio.domain._share.meta.content.SContent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object ContinuePublicationAfterMediaSucceededCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val content = checkNotNull(Mediator.repositories.findOne(SContent.predicateById(request.contentId))) {
                "Content ${request.contentId} was not found."
            }

            when (content.releasePolicy) {
                ReleasePolicy.IMMEDIATE ->
                    Mediator.cmd.send(
                        PublishContentCmd.Request(
                            contentId = request.contentId,
                            publishedAt = LocalDateTime.now(),
                        )
                    )

                ReleasePolicy.GATED -> {
                    Mediator.cmd.send(
                        OpenPublicationReleaseReadinessCmd.Request(
                            contentId = request.contentId,
                            mediaProcessingTaskId = request.mediaProcessingTaskId,
                            releaseWindowOpensAt = requireNotNull(content.releaseWindowOpensAt) {
                                "Gated content ${request.contentId} must have release window opensAt."
                            },
                            releaseWindowClosesAt = requireNotNull(content.releaseWindowClosesAt) {
                                "Gated content ${request.contentId} must have release window closesAt."
                            },
                        )
                    )
                    val sagaId = Mediator.requests.async(
                        PublicationReleaseSaga.Request(
                            contentId = request.contentId,
                        )
                    )
                    Mediator.cmd.send(
                        RegisterPublicationReleaseSagaCmd.Request(
                            contentId = request.contentId,
                            sagaId = sagaId,
                        )
                    )
                }
            }

            return Response
        }
    }

    data class Request(
        val contentId: UUID,
        val mediaProcessingTaskId: UUID
    ) : RequestParam<Response>

    data object Response

}
