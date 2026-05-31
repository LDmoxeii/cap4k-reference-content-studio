package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.events

import com.only4.cap4k.ddd.core.annotation.BuildingBlock
import com.only4.cap4k.ddd.core.domain.event.annotation.DomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId

@DomainEvent(persist = true)
@BuildingBlock(
    tag = "domain_event",
    name = "MediaProcessingSucceeded",
    packageName = "media.processing",
    description = "media processing succeeded",
    aggregates = ["MediaProcessingTask"],
    eventName = "",
    family = "domain-event",
    variant = ""
)
class MediaProcessingSucceededDomainEvent(
    val entity: MediaProcessingTask,
    val taskId: MediaProcessingTaskId,
    val contentId: ContentId,
)
