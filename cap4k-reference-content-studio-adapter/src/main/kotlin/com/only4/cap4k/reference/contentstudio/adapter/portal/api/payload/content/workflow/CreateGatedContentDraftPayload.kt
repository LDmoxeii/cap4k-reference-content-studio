
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.workflow

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

object CreateGatedContentDraftPayload {

    @Schema(name = "CreateGatedContentDraftPayloadRequest")
    data class Request(
        val title: String,
        val body: String,
        val mediaSourceKey: String,
        val releaseWindowOpensAt: LocalDateTime,
        val releaseWindowClosesAt: LocalDateTime
    )

    @Schema(name = "CreateGatedContentDraftPayloadResponse")
    data class Response(
        val contentId: UUID
    )

}
