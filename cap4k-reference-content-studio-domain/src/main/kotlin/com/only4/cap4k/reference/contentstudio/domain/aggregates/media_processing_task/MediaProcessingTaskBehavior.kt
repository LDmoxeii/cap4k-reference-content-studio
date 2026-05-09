package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task

import com.only4.cap4k.ddd.core.domain.event.DomainEventSupervisorSupport.events
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.events.MediaProcessingSucceededDomainEvent

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

fun MediaProcessingTask.markSucceeded() {
    if (processingStatus == MediaProcessingStatus.SUCCEEDED) {
        return
    }

    check(processingStatus == MediaProcessingStatus.SUBMITTED) {
        "Cannot mark a media processing task as succeeded before it has been submitted."
    }

    check(!externalTaskId.isNullOrBlank()) {
        "Cannot mark a media processing task as succeeded without an external task id."
    }

    processingStatus = MediaProcessingStatus.SUCCEEDED
    events().attach(this) {
        MediaProcessingSucceededDomainEvent(
            entity = this,
            taskId = id,
            contentId = contentId,
            externalTaskId = externalTaskId,
        )
    }
}
