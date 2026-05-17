package com.only4.cap4k.reference.contentstudio.application.commands.media.processing

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.media.processing.GetMediaProcessingStatusCli
import org.springframework.stereotype.Service

object RefreshMediaProcessingTaskStatusCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            check(request.externalTaskId.isNotBlank()) {
                "External task id must not be blank."
            }

            val status =
                Mediator.requests.send(
                    GetMediaProcessingStatusCli.Request(request.externalTaskId)
                )
            if (status.status == "SUCCEEDED") {
                Mediator.cmd.send(
                    MarkMediaProcessingSucceededCmd.Request(
                        externalTaskId = request.externalTaskId,
                        assetSha256 = requireNotNull(status.assetSha256) {
                            "Succeeded media processing status must include asset SHA-256."
                        },
                        assetLocation = requireNotNull(status.assetLocation) {
                            "Succeeded media processing status must include asset location."
                        },
                        completedAt = requireNotNull(status.completedAt) {
                            "Succeeded media processing status must include completion time."
                        },
                    )
                )
            }

            return Response
        }
    }

    data class Request(
        val externalTaskId: String
    ) : RequestParam<Response>

    data object Response

}
