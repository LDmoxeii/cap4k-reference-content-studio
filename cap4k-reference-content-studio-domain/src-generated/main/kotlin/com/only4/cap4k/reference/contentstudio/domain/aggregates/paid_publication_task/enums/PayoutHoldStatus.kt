package com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums

import jakarta.persistence.AttributeConverter

enum class PayoutHoldStatus(
    val value: Int,
    val description: String
) {

    NONE(0, "None"),

    RESERVED(1, "Reserved"),

    RELEASED(2, "Released"),

    CAPTURED(3, "Captured");

    companion object {
        private val enumMap: Map<Int, PayoutHoldStatus> = entries.associateBy { it.value }

        fun valueOfOrNull(value: Int?): PayoutHoldStatus? = enumMap[value]
    }

    @jakarta.persistence.Converter(autoApply = false)
    class Converter : AttributeConverter<PayoutHoldStatus, Int> {
        override fun convertToDatabaseColumn(attribute: PayoutHoldStatus?): Int? {
            return attribute?.value
        }

        override fun convertToEntityAttribute(dbData: Int?): PayoutHoldStatus? {
            return valueOfOrNull(dbData)
        }
    }
}
