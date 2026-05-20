
package com.only4.cap4k.reference.contentstudio.application.queries.content.read

import com.only4.cap4k.ddd.core.application.RequestParam
import java.time.LocalDateTime
import java.util.UUID

object GetPaidPublicationStatusQry {

    data class Request(
        val contentId: UUID
    ) : RequestParam<Response>

    data class Response(
        val contentId: UUID,
        val taskId: UUID?,
        val paidPublicationStatus: String?,
        val payoutHoldStatus: String?,
        val entitlementPlanStatus: String?,
        val startedAt: LocalDateTime?,
        val publishedAt: LocalDateTime?,
        val completedAt: LocalDateTime?,
        val failedAt: LocalDateTime?,
        val failedReason: String?
    )

}
