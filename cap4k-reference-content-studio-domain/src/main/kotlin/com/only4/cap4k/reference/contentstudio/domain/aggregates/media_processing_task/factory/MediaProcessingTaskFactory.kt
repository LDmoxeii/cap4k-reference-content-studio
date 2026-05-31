package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.factory

import com.only4.cap4k.ddd.core.domain.aggregate.AggregateFactory
import com.only4.cap4k.ddd.core.domain.aggregate.AggregatePayload
import com.only4.cap4k.ddd.core.annotation.AggregateElement
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values.MediaProcessingResultSnapshot
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
@AggregateElement(
    aggregate = "MediaProcessingTask",
    name = "MediaProcessingTaskFactory",
    packageName = "com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.factory",
    description = "",
    type = "factory",
    root = false
)
class MediaProcessingTaskFactory : AggregateFactory<MediaProcessingTaskFactory.Payload, MediaProcessingTask> {

    override fun create(entityPayload: Payload): MediaProcessingTask =
        MediaProcessingTask(
            id = MediaProcessingTaskId.new(),
            contentId = entityPayload.contentId,
            externalTaskId = entityPayload.externalTaskId,
            processingStatus = entityPayload.processingStatus,
            resultSnapshot = entityPayload.resultSnapshot,
            dbCreatedAt = entityPayload.dbCreatedAt,
            dbUpdatedAt = entityPayload.dbUpdatedAt
        )

    data class Payload(
        val contentId: ContentId,
        val externalTaskId: String?,
        val processingStatus: MediaProcessingStatus,
        val resultSnapshot: MediaProcessingResultSnapshot?,
        val dbCreatedAt: LocalDateTime,
        val dbUpdatedAt: LocalDateTime
    ) : AggregatePayload<MediaProcessingTask>

}
