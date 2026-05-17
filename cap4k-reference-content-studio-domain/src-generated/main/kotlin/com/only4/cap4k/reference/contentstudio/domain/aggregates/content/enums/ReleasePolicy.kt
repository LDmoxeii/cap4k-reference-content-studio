package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums

import jakarta.persistence.AttributeConverter

enum class ReleasePolicy(
    val value: Int,
    val description: String
) {

    IMMEDIATE(0, "Immediate"),

    PAID(1, "Paid");

    companion object {
        private val enumMap: Map<Int, ReleasePolicy> = entries.associateBy { it.value }

        fun valueOfOrNull(value: Int?): ReleasePolicy? = enumMap[value]
    }

    @jakarta.persistence.Converter(autoApply = false)
    class Converter : AttributeConverter<ReleasePolicy, Int> {
        override fun convertToDatabaseColumn(attribute: ReleasePolicy?): Int? {
            return attribute?.value
        }

        override fun convertToEntityAttribute(dbData: Int?): ReleasePolicy? {
            return valueOfOrNull(dbData)
        }
    }
}
