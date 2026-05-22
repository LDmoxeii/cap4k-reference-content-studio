package com.only4.cap4k.reference.contentstudio.application.commands.content.workflow

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.domain._share.meta.content.SContent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.recordMediaReady
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object RecordContentMediaReadyCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val content = checkNotNull(Mediator.repositories.findOne(SContent.predicateById(request.contentId), persist = true)) {
                "Content ${request.contentId} was not found."
            }
            if (content.mediaReadyAt != null) {
                return Response(recorded = false)
            }

            content.recordMediaReady(request.mediaReadyAt)
            Mediator.uow.save()

            return Response(
                recorded = true
            )
        }
    }

    data class Request(
        val contentId: UUID,
        val mediaReadyAt: LocalDateTime
    ) : RequestParam<Response>

    data class Response(
        val recorded: Boolean
    )

}
