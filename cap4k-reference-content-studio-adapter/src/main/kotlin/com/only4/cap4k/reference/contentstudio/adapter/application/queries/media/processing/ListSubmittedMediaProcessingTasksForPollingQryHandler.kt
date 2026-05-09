package com.only4.cap4k.reference.contentstudio.adapter.application.queries.media.processing

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.query.Query
import com.only4.cap4k.reference.contentstudio.application.queries.media.processing.ListSubmittedMediaProcessingTasksForPollingQry
import com.only4.cap4k.reference.contentstudio.domain._share.meta.media_processing_task.SMediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import org.springframework.stereotype.Service

@Service
class ListSubmittedMediaProcessingTasksForPollingQryHandler :
    Query<ListSubmittedMediaProcessingTasksForPollingQry.Request, ListSubmittedMediaProcessingTasksForPollingQry.Response> {

    override fun exec(request: ListSubmittedMediaProcessingTasksForPollingQry.Request): ListSubmittedMediaProcessingTasksForPollingQry.Response {
        val items =
            Mediator.repositories.find(
                SMediaProcessingTask.predicate { schema ->
                    schema.processingStatus.eq(MediaProcessingStatus.SUBMITTED)
                },
                persist = false,
            ).mapNotNull { task ->
                val externalTaskId = task.externalTaskId ?: return@mapNotNull null
                ListSubmittedMediaProcessingTasksForPollingQry.Response.TaskItem(
                    contentId = task.contentId,
                    externalTaskId = externalTaskId,
                )
            }
        return ListSubmittedMediaProcessingTasksForPollingQry.Response(items = items)
    }
}
