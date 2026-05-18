package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task

import com.only4.cap4k.ddd.core.domain.event.DomainEventSupervisorSupport.events
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.events.MediaProcessingSucceededDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values.MediaProcessingResultSnapshot

fun MediaProcessingTask.markSubmitted(externalTaskId: String) {
    check(externalTaskId.isNotBlank()) {
        "Cannot submit a media processing task with a blank external task id."
    }

    when (processingStatus) {
        MediaProcessingStatus.PENDING -> {
            this.externalTaskId = externalTaskId
            processingStatus = MediaProcessingStatus.SUBMITTED
        }

        MediaProcessingStatus.SUBMITTED -> {
            check(this.externalTaskId == externalTaskId) {
                "Cannot resubmit a media processing task with a different external task id."
            }
        }

        MediaProcessingStatus.SUCCEEDED -> {
            error("Cannot submit a media processing task that has already succeeded.")
        }
    }
}

fun MediaProcessingTask.markSucceeded(resultSnapshot: MediaProcessingResultSnapshot) {
    if (processingStatus == MediaProcessingStatus.SUCCEEDED) {
        return
    }

    check(processingStatus == MediaProcessingStatus.SUBMITTED) {
        "Cannot mark a media processing task as succeeded before it has been submitted."
    }

    check(!externalTaskId.isNullOrBlank()) {
        "Cannot mark a media processing task as succeeded without an external task id."
    }

    check(resultSnapshot.mediaProcessingTaskId == id) {
        "Media processing result snapshot does not belong to this task."
    }

    check(resultSnapshot.externalTaskId == externalTaskId) {
        "Media processing result snapshot external task id does not match this task."
    }

    processingStatus = MediaProcessingStatus.SUCCEEDED
    this.resultSnapshot = resultSnapshot
    events().attach(this) {
        MediaProcessingSucceededDomainEvent(
            entity = this,
            taskId = id,
            contentId = contentId,
        )
    }
}
