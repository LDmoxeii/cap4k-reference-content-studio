
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.read

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

object GetContentDetailPayload {

    @Schema(name = "GetContentDetailPayloadRequest")
    data class Request(
        val contentId: UUID
    )

    @Schema(name = "GetContentDetailPayloadResponse")
    data class Response(
        val contentId: UUID,
        val title: String,
        val body: String,
        val mediaSourceKey: String,
        val reviewStatus: String,
        val contentStatus: String,
        val releasePolicy: String?,
        val reviewerId: UUID?,
        val reviewedAt: LocalDateTime?,
        val mediaReadyAt: LocalDateTime?,
        val publishedAt: LocalDateTime?
    )

}
