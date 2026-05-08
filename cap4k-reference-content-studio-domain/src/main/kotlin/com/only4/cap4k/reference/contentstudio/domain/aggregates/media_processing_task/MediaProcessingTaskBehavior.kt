package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task

import com.only4.cap4k.ddd.core.domain.event.DomainEventSupervisorSupport.events
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.events.MediaProcessingSucceededDomainEvent

var MediaProcessingTask.processingStatusValue: MediaProcessingStatus
    get() = MediaProcessingStatus.from(processingStatus)
    internal set(value) {
        processingStatus = value.name
    }

fun MediaProcessingTask.markSubmitted(externalTaskId: String) {
    this.externalTaskId = externalTaskId
    processingStatusValue = MediaProcessingStatus.SUBMITTED
}

fun MediaProcessingTask.markSucceeded() {
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
