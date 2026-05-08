package com.only4.cap4k.reference.contentstudio.application.ports

import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import java.util.UUID

interface MediaProcessingTaskRepository {
    fun findByContentId(contentId: UUID): MediaProcessingTask?

    fun save(task: MediaProcessingTask): MediaProcessingTask
}
