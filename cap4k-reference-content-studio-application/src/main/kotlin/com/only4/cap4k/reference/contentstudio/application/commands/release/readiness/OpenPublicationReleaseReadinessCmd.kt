package com.only4.cap4k.reference.contentstudio.application.commands.release.readiness

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object OpenPublicationReleaseReadinessCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            Mediator.uow.save()

            return Response(
                readinessId = TODO("set readinessId")
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
