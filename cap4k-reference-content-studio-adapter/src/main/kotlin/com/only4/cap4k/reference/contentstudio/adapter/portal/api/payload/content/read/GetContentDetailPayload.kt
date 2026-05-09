
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.read

import java.time.LocalDateTime
import java.util.UUID

object GetContentDetailPayload {

    data class Request(
        val contentId: UUID
    )

    data class Response(
        val contentId: UUID,
        val title: String,
        val body: String,
        val mediaSourceKey: String,
        val reviewStatus: String,
        val contentStatus: String,
        val reviewerId: UUID?,
        val reviewedAt: LocalDateTime?,
        val publishedAt: LocalDateTime?
    )

}
