
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.read

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

object GetPaidPublicationStatusPayload {

    @Schema(name = "GetPaidPublicationStatusPayloadRequest")
    data class Request(
        val contentId: UUID
    )

    @Schema(name = "GetPaidPublicationStatusPayloadResponse")
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
