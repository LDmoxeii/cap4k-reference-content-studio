package com.only4.cap4k.reference.contentstudio.application.commands.content.workflow

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object CreateGatedContentDraftCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            Mediator.uow.save()

            return Response(
                contentId = TODO("set contentId")
            )
        }
    }

    data class Request(
        val title: String,
        val body: String,
        val mediaSourceKey: String,
        val releaseWindowOpensAt: LocalDateTime,
        val releaseWindowClosesAt: LocalDateTime
    ) : RequestParam<Response>

    data class Response(
        val contentId: UUID
    )

}
