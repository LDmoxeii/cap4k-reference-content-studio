package com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums

import jakarta.persistence.AttributeConverter

enum class CopyrightReviewStatus(
    val value: Int,
    val description: String
) {

    WAITING(0, "Waiting"),

    PASSED(1, "Passed"),

    REJECTED(2, "Rejected");

    companion object {
        private val enumMap: Map<Int, CopyrightReviewStatus> = entries.associateBy { it.value }

        fun valueOfOrNull(value: Int?): CopyrightReviewStatus? = enumMap[value]
    }

    @jakarta.persistence.Converter(autoApply = false)
    class Converter : AttributeConverter<CopyrightReviewStatus, Int> {
        override fun convertToDatabaseColumn(attribute: CopyrightReviewStatus?): Int? {
            return attribute?.value
        }

        override fun convertToEntityAttribute(dbData: Int?): CopyrightReviewStatus? {
            return valueOfOrNull(dbData)
        }
    }
}
