package com.only4.cap4k.reference.contentstudio.application.jobs

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.MarkMediaProcessingSucceededCmd
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.media.processing.GetMediaProcessingStatusCli
import com.only4.cap4k.reference.contentstudio.application.queries.media.processing.ListSubmittedMediaProcessingTasksForPollingQry
import org.springframework.stereotype.Service

/**
 * Callback remains the main path. This job only translates external polling observations
 * back into the same internal transition surface when callback is unavailable.
 */
@Service
class MediaProcessingPollingFallbackJob {
    fun pollSubmittedTasks() {
        Mediator.qry.send(ListSubmittedMediaProcessingTasksForPollingQry.Request()).items.forEach { task ->
            val externalTaskId = task.externalTaskId
            val status = Mediator.requests.send(GetMediaProcessingStatusCli.Request(externalTaskId))
            if (status.status == "SUCCEEDED") {
                Mediator.cmd.send(
                    MarkMediaProcessingSucceededCmd.Request(
                        externalTaskId = externalTaskId,
                        assetSha256 = requireNotNull(status.assetSha256) {
                            "Succeeded media processing status must include asset SHA-256."
                        },
                        assetLocation = requireNotNull(status.assetLocation) {
                            "Succeeded media processing status must include asset location."
                        },
                        completedAt = requireNotNull(status.completedAt) {
                            "Succeeded media processing status must include completion time."
                        },
                    )
                )
            }
        }
    }
}
