package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums

import jakarta.persistence.AttributeConverter

enum class MediaProcessingStatus(
    val value: Int,
    val description: String
) {

    PENDING(0, "Pending"),

    SUBMITTED(1, "Submitted"),

    SUCCEEDED(2, "Succeeded");

    companion object {
        private val enumMap: Map<Int, MediaProcessingStatus> = entries.associateBy { it.value }

        fun valueOfOrNull(value: Int?): MediaProcessingStatus? = enumMap[value]
    }

    @jakarta.persistence.Converter(autoApply = false)
    class Converter : AttributeConverter<MediaProcessingStatus, Int> {
        override fun convertToDatabaseColumn(attribute: MediaProcessingStatus?): Int? {
            return attribute?.value
        }

        override fun convertToEntityAttribute(dbData: Int?): MediaProcessingStatus? {
            return valueOfOrNull(dbData)
        }
    }
}
