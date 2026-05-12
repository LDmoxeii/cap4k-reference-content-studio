package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task

import com.only4.cap4k.ddd.core.domain.id.ApplicationSideId
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "media_processing_task")
class MediaProcessingTask(
    id: UUID = UUID(0L, 0L),
    contentId: UUID,
    externalTaskId: String? = null,
    processingStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus,
    resultSnapshot: com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values.MediaProcessingResultSnapshot? = null,
    dbCreatedAt: java.time.LocalDateTime,
    dbUpdatedAt: java.time.LocalDateTime
) {

    @Id
    @field:ApplicationSideId(strategy = "uuid7")
    @Column(name = "id", insertable = true, updatable = false)
    var id: UUID = id
        internal set

    @Column(name = "content_id")
    var contentId: UUID = contentId
        internal set

    @Column(name = "external_task_id")
    var externalTaskId: String? = externalTaskId
        internal set

    @Column(name = "processing_status")
    @Convert(converter = com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus.Converter::class)
    var processingStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus = processingStatus
        internal set

    @Column(name = "result_snapshot")
    @Convert(converter = com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values.MediaProcessingResultSnapshotConverter::class)
    var resultSnapshot: com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values.MediaProcessingResultSnapshot? = resultSnapshot
        internal set

    @Column(name = "db_created_at")
    var dbCreatedAt: java.time.LocalDateTime = dbCreatedAt
        internal set

    @Column(name = "db_updated_at")
    var dbUpdatedAt: java.time.LocalDateTime = dbUpdatedAt
        internal set

}
