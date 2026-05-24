
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.read

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId

object GetPaidPublicationStatusPayload {

    @Schema(name = "GetPaidPublicationStatusPayloadRequest")
    data class Request(
        val contentId: ContentId
    )

    @Schema(name = "GetPaidPublicationStatusPayloadResponse")
    data class Response(
        val contentId: ContentId,
        val taskId: PaidPublicationTaskId?,
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
