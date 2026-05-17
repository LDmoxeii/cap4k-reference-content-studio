package com.only4.cap4k.reference.contentstudio.application.commands.release.readiness

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.sagas.publication.PublicationReleaseSaga
import com.only4.cap4k.reference.contentstudio.domain._share.meta.publication_release_readiness.SPublicationReleaseReadiness
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.canStartPublicationReleaseSaga
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.recordPublicationReleaseSagaStarted
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object TryContinuePublicationReleaseCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val readiness =
                checkNotNull(
                    Mediator.repositories.findFirst(
                        SPublicationReleaseReadiness.predicate { schema ->
                            schema.contentId.eq(request.contentId)
                        }
                    )
                ) {
                    "Publication release readiness for content ${request.contentId} was not found."
                }
            val now = LocalDateTime.now()
            if (!readiness.canStartPublicationReleaseSaga(now)) {
                return Response(continued = false)
            }

            val sagaId =
                Mediator.requests.async(
                    PublicationReleaseSaga.Request(
                        contentId = request.contentId,
                    )
                )
            readiness.recordPublicationReleaseSagaStarted(sagaId, now)
            Mediator.uow.save()

            return Response(continued = true)
        }
    }

    data class Request(
        val contentId: UUID
    ) : RequestParam<Response>

    data class Response(
        val continued: Boolean
    )

}
