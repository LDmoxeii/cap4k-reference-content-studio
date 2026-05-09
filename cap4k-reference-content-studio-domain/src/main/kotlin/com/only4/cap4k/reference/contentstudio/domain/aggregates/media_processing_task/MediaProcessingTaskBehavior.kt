package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task

import com.only4.cap4k.ddd.core.domain.event.DomainEventSupervisorSupport.events
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.events.MediaProcessingSucceededDomainEvent

var MediaProcessingTask.processingStatusValue: MediaProcessingStatus
    get() = processingStatus
    internal set(value) {
        processingStatus = value
    }

fun MediaProcessingTask.markSubmitted(externalTaskId: String) {
    check(externalTaskId.isNotBlank()) {
        "Cannot submit a media processing task with a blank external task id."
    }

    when (processingStatusValue) {
        MediaProcessingStatus.PENDING -> {
            this.externalTaskId = externalTaskId
            processingStatusValue = MediaProcessingStatus.SUBMITTED
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
    if (processingStatusValue == MediaProcessingStatus.SUCCEEDED) {
        return
    }

    check(processingStatusValue == MediaProcessingStatus.SUBMITTED) {
        "Cannot mark a media processing task as succeeded before it has been submitted."
    }

    check(!externalTaskId.isNullOrBlank()) {
        "Cannot mark a media processing task as succeeded without an external task id."
    }

    processingStatusValue = MediaProcessingStatus.SUCCEEDED
    events().attach(this) {
        MediaProcessingSucceededDomainEvent(
            entity = this,
            taskId = id,
            contentId = contentId,
            externalTaskId = externalTaskId,
        )
    }
}
