package com.only4.cap4k.reference.contentstudio.application.queries.release.readiness

import com.only4.cap4k.ddd.core.application.RequestParam
import java.time.LocalDateTime
import java.util.UUID

object ListPublicationReleaseReadinessReadyToContinueQry {

    data class Request(
        val now: LocalDateTime
    ) : RequestParam<Response>

    data class Response(
        val items: List<ReadyItem>
    ) {
        data class ReadyItem(
            val contentId: UUID
        )
    }
}
