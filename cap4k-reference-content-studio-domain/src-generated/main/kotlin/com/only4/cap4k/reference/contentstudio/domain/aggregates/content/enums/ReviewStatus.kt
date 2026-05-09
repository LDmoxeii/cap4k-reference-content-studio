package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums

import jakarta.persistence.AttributeConverter

enum class ReviewStatus(
    val value: Int,
    val description: String
) {

    PENDING(0, "Pending"),

    APPROVED(1, "Approved");

    companion object {
        private val enumMap: Map<Int, ReviewStatus> = entries.associateBy { it.value }

        fun valueOfOrNull(value: Int?): ReviewStatus? = enumMap[value]
    }

    @jakarta.persistence.Converter(autoApply = false)
    class Converter : AttributeConverter<ReviewStatus, Int> {
        override fun convertToDatabaseColumn(attribute: ReviewStatus?): Int? {
            return attribute?.value
        }

        override fun convertToEntityAttribute(dbData: Int?): ReviewStatus? {
            return valueOfOrNull(dbData)
        }
    }
}
