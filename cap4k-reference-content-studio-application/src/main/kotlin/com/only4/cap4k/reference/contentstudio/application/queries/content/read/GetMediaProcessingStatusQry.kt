
package com.only4.cap4k.reference.contentstudio.application.queries.content.read

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId

object GetMediaProcessingStatusQry {

    data class Request(
        val contentId: ContentId
    ) : RequestParam<Response>

    data class Response(
        val contentId: ContentId,
        val task: TaskSnapshot?
    ) {
        data class TaskSnapshot(
            val taskId: MediaProcessingTaskId,
            val externalTaskId: String?,
            val processingStatus: String
        )
    }

}
