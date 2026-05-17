package com.only4.cap4k.reference.contentstudio.application.jobs

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.RefreshMediaProcessingTaskStatusCmd
import com.only4.cap4k.reference.contentstudio.application.queries.media.processing.ListSubmittedMediaProcessingTasksForPollingQry
import org.springframework.stereotype.Service

/**
 * Callback remains the main path. This job only translates external polling observations
 * back into the same internal transition surface when callback is unavailable.
 */
@Service
class MediaProcessingPollingFallbackJob {
    fun pollSubmittedTasks() {
        val submittedTasks =
            Mediator.qry.send(ListSubmittedMediaProcessingTasksForPollingQry.Request()).items

        submittedTasks.forEach { task ->
            Mediator.cmd.send(
                RefreshMediaProcessingTaskStatusCmd.Request(
                    externalTaskId = task.externalTaskId,
                )
            )
        }
    }
}
