package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums

import jakarta.persistence.AttributeConverter

enum class ContentStatus(
    val value: Int,
    val description: String
) {

    DRAFT(0, "Draft"),

    PUBLISHED(1, "Published");

    companion object {
        private val enumMap: Map<Int, ContentStatus> = entries.associateBy { it.value }

        fun valueOfOrNull(value: Int?): ContentStatus? = enumMap[value]
    }

    @jakarta.persistence.Converter(autoApply = false)
    class Converter : AttributeConverter<ContentStatus, Int> {
        override fun convertToDatabaseColumn(attribute: ContentStatus?): Int? {
            return attribute?.value
        }

        override fun convertToEntityAttribute(dbData: Int?): ContentStatus? {
            return valueOfOrNull(dbData)
        }
    }
}
