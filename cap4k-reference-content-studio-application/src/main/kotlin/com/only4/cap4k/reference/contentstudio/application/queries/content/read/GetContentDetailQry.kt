
package com.only4.cap4k.reference.contentstudio.application.queries.content.read

import com.only4.cap4k.ddd.core.application.RequestParam
import java.time.LocalDateTime
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId

object GetContentDetailQry {

    data class Request(
        val contentId: ContentId
    ) : RequestParam<Response>

    data class Response(
        val contentId: ContentId,
        val title: String,
        val body: String,
        val mediaSourceKey: String,
        val reviewStatus: String,
        val contentStatus: String,
        val releasePolicy: String,
        val reviewerId: ReviewerId?,
        val reviewedAt: LocalDateTime?,
        val mediaReadyAt: LocalDateTime?,
        val publishedAt: LocalDateTime?
    )

}
