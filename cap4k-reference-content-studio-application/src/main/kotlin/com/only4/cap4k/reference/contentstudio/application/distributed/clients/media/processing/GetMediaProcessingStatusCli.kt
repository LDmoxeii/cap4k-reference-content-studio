package com.only4.cap4k.reference.contentstudio.application.distributed.clients.media.processing

import com.only4.cap4k.ddd.core.application.RequestParam
import java.time.LocalDateTime
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

@BuildingBlock(
    tag = "client",
    name = "GetMediaProcessingStatus",
    packageName = "media.processing",
    description = "get media processing status from fake external cli",
    aggregates = ["MediaProcessingTask"],
    family = "client"
)
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
