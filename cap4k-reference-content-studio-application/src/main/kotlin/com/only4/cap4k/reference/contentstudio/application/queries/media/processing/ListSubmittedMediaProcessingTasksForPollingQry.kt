package com.only4.cap4k.reference.contentstudio.application.queries.media.processing

import com.only4.cap4k.ddd.core.application.RequestParam
import java.util.UUID

object ListSubmittedMediaProcessingTasksForPollingQry {

    class Request : RequestParam<Response>

    data class Response(
        val items: List<TaskItem>
    ) {
        data class TaskItem(
            val contentId: UUID,
            val externalTaskId: String
        )
    }
}
