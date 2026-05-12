package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = false)
class MediaProcessingResultSnapshotConverter : AttributeConverter<MediaProcessingResultSnapshot, String> {
    override fun convertToDatabaseColumn(attribute: MediaProcessingResultSnapshot?): String? =
        attribute?.let { objectMapper.writeValueAsString(it) }

    override fun convertToEntityAttribute(dbData: String?): MediaProcessingResultSnapshot? =
        dbData?.takeIf { it.isNotBlank() }?.let { objectMapper.readValue(it) }

    companion object {
        private val objectMapper: ObjectMapper =
            ObjectMapper()
                .registerModule(KotlinModule.Builder().build())
                .registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}
