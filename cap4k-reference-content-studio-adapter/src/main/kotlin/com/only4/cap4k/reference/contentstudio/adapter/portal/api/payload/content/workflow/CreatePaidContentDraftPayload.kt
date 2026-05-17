
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.workflow

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

object CreatePaidContentDraftPayload {

    @Schema(name = "CreatePaidContentDraftPayloadRequest")
    data class Request(
        val title: String,
        val body: String,
        val mediaSourceKey: String
    )

    @Schema(name = "CreatePaidContentDraftPayloadResponse")
    data class Response(
        val contentId: UUID
    )

}
