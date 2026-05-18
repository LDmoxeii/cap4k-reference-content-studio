package com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums

import jakarta.persistence.AttributeConverter

enum class EntitlementPlanStatus(
    val value: Int,
    val description: String
) {

    NONE(0, "None"),

    CREATED(1, "Created"),

    ACTIVATED(2, "Activated"),

    CANCELLED(3, "Cancelled");

    companion object {
        private val enumMap: Map<Int, EntitlementPlanStatus> = entries.associateBy { it.value }

        fun valueOfOrNull(value: Int?): EntitlementPlanStatus? = enumMap[value]
    }

    @jakarta.persistence.Converter(autoApply = false)
    class Converter : AttributeConverter<EntitlementPlanStatus, Int> {
        override fun convertToDatabaseColumn(attribute: EntitlementPlanStatus?): Int? {
            return attribute?.value
        }

        override fun convertToEntityAttribute(dbData: Int?): EntitlementPlanStatus? {
            return valueOfOrNull(dbData)
        }
    }
}
