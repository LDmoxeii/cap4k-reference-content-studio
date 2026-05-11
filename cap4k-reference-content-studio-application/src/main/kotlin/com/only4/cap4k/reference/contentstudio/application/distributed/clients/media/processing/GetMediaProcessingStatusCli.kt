package com.only4.cap4k.reference.contentstudio.application.distributed.clients.media.processing

import com.only4.cap4k.ddd.core.application.RequestParam
import java.time.LocalDateTime

object GetMediaProcessingStatusCli {

    data class Request(
        val externalTaskId: String
    ) : RequestParam<Response>

    data class Response(
        val status: String,
        val assetSha256: String?,
        val assetLocation: String?,
        val completedAt: LocalDateTime?,
    )

}
