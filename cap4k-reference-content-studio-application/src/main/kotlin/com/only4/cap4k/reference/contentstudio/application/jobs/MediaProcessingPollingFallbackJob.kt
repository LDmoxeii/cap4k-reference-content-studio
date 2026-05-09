package com.only4.cap4k.reference.contentstudio.application.jobs

import com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingCli
import com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingTaskRepository
import com.only4.cap4k.reference.contentstudio.application.transition.MediaProcessingSucceededTransitionSurface
import org.springframework.stereotype.Service

/**
 * Callback remains the main path. This job only translates external polling observations
 * back into the same internal transition surface when callback is unavailable.
 */
@Service
class MediaProcessingPollingFallbackJob(
    private val mediaProcessingTaskRepository: MediaProcessingTaskRepository,
    private val mediaProcessingCli: MediaProcessingCli,
    private val transitionSurface: MediaProcessingSucceededTransitionSurface,
) {
    fun pollSubmittedTasks() {
        mediaProcessingTaskRepository.findSubmittedTasks().forEach { task ->
            val externalTaskId = task.externalTaskId ?: return@forEach
            val status = mediaProcessingCli.getStatus(externalTaskId)
            if (status.status == MediaProcessingCli.ExternalTaskStatus.SUCCEEDED) {
                transitionSurface.on(
                    MediaProcessingSucceededTransitionSurface.Event(
                        externalTaskId = externalTaskId,
                    )
                )
            }
        }
    }
}
