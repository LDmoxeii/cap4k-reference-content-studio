package com.only4.cap4k.reference.contentstudio.application.commands.media.processing

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.domain._share.meta.media_processing_task.SMediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.markSucceeded
import org.springframework.stereotype.Service

object MarkMediaProcessingSucceededCmd {

    @Service
    open class Handler : Command<Request, Response> {

        open override fun exec(request: Request): Response {
            check(request.externalTaskId.isNotBlank()) {
                "External task id must not be blank."
            }
            val task =
                checkNotNull(
                    Mediator.repositories.findFirst(
                        SMediaProcessingTask.predicate { schema ->
                            schema.externalTaskId.eq(request.externalTaskId)
                        }
                    )
                ) {
                "Media processing task for external task id ${request.externalTaskId} was not found."
            }
            task.markSucceeded()
            Mediator.uow.save()

            return Response
        }
    }

    data class Request(
        val externalTaskId: String
    ) : RequestParam<Response>

    data object Response

}
