package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.factory

import com.only4.cap4k.ddd.core.domain.aggregate.AggregateFactory
import com.only4.cap4k.ddd.core.domain.aggregate.AggregatePayload
import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values.MediaProcessingResultSnapshot
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
@Aggregate(
    aggregate = "MediaProcessingTask",
    name = "MediaProcessingTaskFactory",
    type = Aggregate.TYPE_FACTORY,
    description = ""
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

    @Aggregate(
        aggregate = "MediaProcessingTask",
        name = "MediaProcessingTaskPayload",
        type = Aggregate.TYPE_FACTORY_PAYLOAD,
        description = ""
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
