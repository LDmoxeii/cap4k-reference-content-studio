
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.workflow

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

object ApproveContentReviewPayload {

    @Schema(name = "ApproveContentReviewPayloadRequest")
    data class Request(
        val reviewerId: UUID
    )

    @Schema(name = "ApproveContentReviewPayloadResponse")
    class Response

}
