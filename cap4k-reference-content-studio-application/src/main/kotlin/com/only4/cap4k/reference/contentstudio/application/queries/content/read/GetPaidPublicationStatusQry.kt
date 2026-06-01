
package com.only4.cap4k.reference.contentstudio.application.queries.content.read

import com.only4.cap4k.ddd.core.application.RequestParam
import java.time.LocalDateTime
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

@BuildingBlock(
    tag = "query",
    name = "GetPaidPublicationStatus",
    packageName = "content.read",
    description = "get paid publication status",
    aggregates = ["PaidPublicationTask"],
    family = "query"
)
object GetPaidPublicationStatusQry {

    data class Request(
        val contentId: ContentId
    ) : RequestParam<Response>

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
