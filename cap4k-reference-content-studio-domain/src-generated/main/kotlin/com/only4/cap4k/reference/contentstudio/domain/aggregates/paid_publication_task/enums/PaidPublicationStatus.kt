package com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums

import jakarta.persistence.AttributeConverter

enum class PaidPublicationStatus(
    val value: Int,
    val description: String
) {

    PENDING(0, "Pending"),

    RUNNING(1, "Running"),

    PUBLISHED(2, "Published"),

    FAILED(3, "Failed"),

    REQUIRES_OPERATOR_REPAIR(4, "Requires operator repair");

    companion object {
        private val enumMap: Map<Int, PaidPublicationStatus> = entries.associateBy { it.value }

        fun valueOfOrNull(value: Int?): PaidPublicationStatus? = enumMap[value]
    }

    @jakarta.persistence.Converter(autoApply = false)
    class Converter : AttributeConverter<PaidPublicationStatus, Int> {
        override fun convertToDatabaseColumn(attribute: PaidPublicationStatus?): Int? {
            return attribute?.value
        }

        override fun convertToEntityAttribute(dbData: Int?): PaidPublicationStatus? {
            return valueOfOrNull(dbData)
        }
    }
}
