package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.factory

import com.only4.cap4k.ddd.core.domain.aggregate.AggregateFactory
import com.only4.cap4k.ddd.core.domain.aggregate.AggregatePayload
import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@Aggregate(
    aggregate = "MediaProcessingTask",
    name = "MediaProcessingTaskFactory",
    type = Aggregate.TYPE_FACTORY,
    description = ""
)
class MediaProcessingTaskFactory : AggregateFactory<MediaProcessingTaskFactory.Payload, MediaProcessingTask> {

    override fun create(payload: Payload): MediaProcessingTask {
        TODO("Implement aggregate construction")
    }

    @Aggregate(
        aggregate = "MediaProcessingTask",
        name = "MediaProcessingTaskPayload",
        type = Aggregate.TYPE_FACTORY_PAYLOAD,
        description = ""
    )

    data class Payload(

        val id: UUID,

        val contentId: UUID,

        val externalTaskId: String?,

        val processingStatus: String,

        val dbCreatedAt: java.time.LocalDateTime,

        val dbUpdatedAt: java.time.LocalDateTime

    ) : AggregatePayload<MediaProcessingTask>

}
