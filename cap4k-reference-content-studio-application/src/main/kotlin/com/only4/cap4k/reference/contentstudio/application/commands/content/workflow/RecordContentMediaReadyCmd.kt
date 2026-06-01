package com.only4.cap4k.reference.contentstudio.application.commands.content.workflow

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.domain._share.meta.content.SContent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.recordMediaReady
import java.time.LocalDateTime
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import org.springframework.stereotype.Service
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

@BuildingBlock(
    tag = "command",
    name = "RecordContentMediaReady",
    packageName = "content.workflow",
    description = "record content media readiness",
    aggregates = ["Content"],
    family = "command"
)
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
        val contentId: ContentId,
        val mediaReadyAt: LocalDateTime
    ) : RequestParam<Response>

    data class Response(
        val recorded: Boolean
    )

}
