
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.read

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

object GetContentDetailPayload {

    @Schema(name = "GetContentDetailPayloadRequest")
    data class Request(
        val contentId: ContentId
    )

    @Schema(name = "GetContentDetailPayloadResponse")
    data class Response(
        val contentId: ContentId,
        val title: String,
        val body: String,
        val mediaSourceKey: String,
        val reviewStatus: String,
        val contentStatus: String,
        val releasePolicy: String?,
        val reviewerId: ReviewerId?,
        val reviewedAt: LocalDateTime?,
        val mediaReadyAt: LocalDateTime?,
        val publishedAt: LocalDateTime?
    )

}
