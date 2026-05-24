
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.read

import io.swagger.v3.oas.annotations.media.Schema
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId

object GetMediaProcessingStatusPayload {

    @Schema(name = "GetMediaProcessingStatusPayloadRequest")
    data class Request(
        val contentId: ContentId
    )

    @Schema(name = "GetMediaProcessingStatusPayloadResponse")
    data class Response(
        val contentId: ContentId,
        val task: Task?
    ) {
        @Schema(name = "GetMediaProcessingStatusPayloadResponseTask")
        data class Task(
            val taskId: MediaProcessingTaskId,
            val externalTaskId: String?,
            val processingStatus: String
        )
    }

}
