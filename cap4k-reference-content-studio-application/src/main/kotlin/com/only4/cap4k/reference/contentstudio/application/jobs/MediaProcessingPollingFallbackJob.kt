package com.only4.cap4k.reference.contentstudio.application.jobs

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.MarkMediaProcessingSucceededCmd
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.media.processing.GetMediaProcessingStatusCli
import com.only4.cap4k.reference.contentstudio.domain._share.meta.media_processing_task.SMediaProcessingTask
import org.springframework.stereotype.Service

/**
 * Callback remains the main path. This job only translates external polling observations
 * back into the same internal transition surface when callback is unavailable.
 */
@Service
class MediaProcessingPollingFallbackJob {
    fun pollSubmittedTasks() {
        Mediator.repositories.find(
            SMediaProcessingTask.predicate { schema ->
                schema.processingStatus.eq("SUBMITTED")
            },
            persist = false,
        ).forEach { task ->
            val externalTaskId = task.externalTaskId ?: return@forEach
            val status = Mediator.requests.send(GetMediaProcessingStatusCli.Request(externalTaskId))
            if (status.status == "SUCCEEDED") {
                Mediator.cmd.send(
                    MarkMediaProcessingSucceededCmd.Request(
                        externalTaskId = externalTaskId,
                    )
                )
            }
        }
    }
}
