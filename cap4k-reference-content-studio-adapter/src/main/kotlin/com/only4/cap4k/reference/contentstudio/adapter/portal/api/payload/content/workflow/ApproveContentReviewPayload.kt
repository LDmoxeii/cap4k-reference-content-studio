
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.workflow

import io.swagger.v3.oas.annotations.media.Schema
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId

object ApproveContentReviewPayload {

    @Schema(name = "ApproveContentReviewPayloadRequest")
    data class Request(
        val reviewerId: ReviewerId
    )

    @Schema(name = "ApproveContentReviewPayloadResponse")
    class Response

}
