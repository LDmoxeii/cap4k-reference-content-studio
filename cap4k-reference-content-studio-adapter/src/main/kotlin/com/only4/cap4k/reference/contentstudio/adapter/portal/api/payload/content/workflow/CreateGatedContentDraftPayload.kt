
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.workflow

import java.time.LocalDateTime
import java.util.UUID

object CreateGatedContentDraftPayload {

    data class Request(
        val title: String,
        val body: String,
        val mediaSourceKey: String,
        val releaseWindowOpensAt: LocalDateTime,
        val releaseWindowClosesAt: LocalDateTime
    )

    data class Response(
        val contentId: UUID
    )

}
