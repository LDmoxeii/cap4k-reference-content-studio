package com.only4.cap4k.reference.contentstudio.application.commands.media.processing

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.domain._share.meta.media_processing_task.SMediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.markSucceeded
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values.MediaProcessingResultSnapshot
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values.MediaProcessingResultStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime

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
            val resultSnapshot = MediaProcessingResultSnapshot.create(
                mediaProcessingTaskId = task.id,
                externalTaskId = request.externalTaskId,
                resultStatus = MediaProcessingResultStatus.SUCCEEDED,
                assetSha256 = request.assetSha256,
                assetLocation = request.assetLocation,
                completedAt = request.completedAt,
                now = LocalDateTime.now(),
            )
            task.markSucceeded(resultSnapshot)
            Mediator.uow.persist(resultSnapshot)
            Mediator.uow.save()

            return Response
        }
    }

    data class Request(
        val externalTaskId: String,
        val assetSha256: String,
        val assetLocation: String,
        val completedAt: LocalDateTime,
    ) : RequestParam<Response>

    data object Response

}
