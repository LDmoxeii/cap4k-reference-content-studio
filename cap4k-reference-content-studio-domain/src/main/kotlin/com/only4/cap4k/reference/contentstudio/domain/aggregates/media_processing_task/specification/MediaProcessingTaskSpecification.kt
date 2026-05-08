package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.specification

import com.only4.cap4k.ddd.core.domain.aggregate.Specification
import com.only4.cap4k.ddd.core.domain.aggregate.Specification.Result
import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import org.springframework.stereotype.Service

@Service
@Aggregate(
    aggregate = "MediaProcessingTask",
    name = "MediaProcessingTaskSpecification",
    type = Aggregate.TYPE_SPECIFICATION,
    description = ""
)
class MediaProcessingTaskSpecification : Specification<MediaProcessingTask> {

    override fun specify(entity: MediaProcessingTask): Result {
        return Result.pass()
    }
}
