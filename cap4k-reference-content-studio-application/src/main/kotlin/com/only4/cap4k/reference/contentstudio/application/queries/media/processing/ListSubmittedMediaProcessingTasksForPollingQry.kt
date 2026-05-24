package com.only4.cap4k.reference.contentstudio.application.queries.media.processing

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId

object ListSubmittedMediaProcessingTasksForPollingQry {

    class Request : RequestParam<Response>

    data class Response(
        val items: List<TaskItem>
    ) {
        data class TaskItem(
            val contentId: ContentId,
            val externalTaskId: String
        )
    }
}
