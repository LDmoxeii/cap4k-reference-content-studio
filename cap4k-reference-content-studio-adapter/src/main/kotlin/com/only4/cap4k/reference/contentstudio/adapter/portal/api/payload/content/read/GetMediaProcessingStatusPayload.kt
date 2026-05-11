
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.read

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

object GetMediaProcessingStatusPayload {

    @Schema(name = "GetMediaProcessingStatusPayloadRequest")
    data class Request(
        val contentId: UUID
    )

    @Schema(name = "GetMediaProcessingStatusPayloadResponse")
    data class Response(
        val contentId: UUID,
        val task: Task?
    ) {
        @Schema(name = "GetMediaProcessingStatusPayloadResponseTask")
        data class Task(
            val taskId: UUID,
            val externalTaskId: String?,
            val processingStatus: String
        )
    }

}
