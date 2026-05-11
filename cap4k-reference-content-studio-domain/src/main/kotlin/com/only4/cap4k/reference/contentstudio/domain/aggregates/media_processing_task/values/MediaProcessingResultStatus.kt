package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values

import jakarta.persistence.AttributeConverter

enum class MediaProcessingResultStatus(
    val value: Int,
    val description: String
) {

    SUCCEEDED(0, "Succeeded"),

    FAILED(1, "Failed");

    companion object {
        private val enumMap: Map<Int, MediaProcessingResultStatus> = entries.associateBy { it.value }

        fun valueOfOrNull(value: Int?): MediaProcessingResultStatus? = enumMap[value]
    }

    @jakarta.persistence.Converter(autoApply = false)
    class Converter : AttributeConverter<MediaProcessingResultStatus, Int> {
        override fun convertToDatabaseColumn(attribute: MediaProcessingResultStatus?): Int? {
            return attribute?.value
        }

        override fun convertToEntityAttribute(dbData: Int?): MediaProcessingResultStatus? {
            return valueOfOrNull(dbData)
        }
    }
}
