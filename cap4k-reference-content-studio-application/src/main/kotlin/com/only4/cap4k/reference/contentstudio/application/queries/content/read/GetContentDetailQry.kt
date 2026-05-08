
package com.only4.cap4k.reference.contentstudio.application.queries.content.read

import com.only4.cap4k.ddd.core.application.RequestParam
import java.time.LocalDateTime
import java.util.UUID

object GetContentDetailQry {

    data class Request(
        val contentId: UUID
    ) : RequestParam<Response>

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
