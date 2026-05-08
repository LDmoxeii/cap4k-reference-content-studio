package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.events

import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.ddd.core.domain.event.annotation.DomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import java.util.UUID

@DomainEvent(persist = false)
@Aggregate(
    aggregate = "MediaProcessingTask",
    name = "MediaProcessingSucceededDomainEvent",
    type = Aggregate.TYPE_DOMAIN_EVENT,
    description = "",
)
class MediaProcessingSucceededDomainEvent(
    val entity: MediaProcessingTask,
    val taskId: UUID,
    val contentId: UUID,
    val externalTaskId: String?,
)
