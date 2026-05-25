
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.workflow

import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import io.swagger.v3.oas.annotations.media.Schema

object ApproveContentReviewPayload {

    @Schema(name = "ApproveContentReviewPayloadRequest")
    data class Request(
        val reviewerId: ReviewerId
    )

    @Schema(name = "ApproveContentReviewPayloadResponse")
    class Response

}
