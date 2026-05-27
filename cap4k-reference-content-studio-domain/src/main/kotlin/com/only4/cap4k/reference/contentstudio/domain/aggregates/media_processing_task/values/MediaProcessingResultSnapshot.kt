package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingResultStatus
import jakarta.persistence.AttributeConverter
import java.time.LocalDateTime

data class MediaProcessingResultSnapshot(
    val mediaProcessingTaskId: MediaProcessingTaskId,
    val contentId: ContentId,
    val externalTaskId: String,
    val resultStatus: MediaProcessingResultStatus,
    val assetSha256: String,
    val assetLocation: String,
    val completedAt: LocalDateTime,
    val dbCreatedAt: LocalDateTime,
    val dbUpdatedAt: LocalDateTime
) {
    @jakarta.persistence.Converter(autoApply = false)
    class Converter : AttributeConverter<MediaProcessingResultSnapshot, String> {
        override fun convertToDatabaseColumn(attribute: MediaProcessingResultSnapshot?): String? =
            if (attribute == null) null else mapper.writeValueAsString(attribute)

        override fun convertToEntityAttribute(dbData: String?): MediaProcessingResultSnapshot? =
            dbData?.let { mapper.readValue<MediaProcessingResultSnapshot>(it) }
    }

    companion object {
        private val mapper: ObjectMapper = ObjectMapper().findAndRegisterModules()
    }
}
