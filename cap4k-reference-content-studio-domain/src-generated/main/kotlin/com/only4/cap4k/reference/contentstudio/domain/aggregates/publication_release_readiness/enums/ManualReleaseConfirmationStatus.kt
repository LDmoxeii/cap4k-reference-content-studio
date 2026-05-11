package com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums

import jakarta.persistence.AttributeConverter

enum class ManualReleaseConfirmationStatus(
    val value: Int,
    val description: String
) {

    WAITING(0, "Waiting"),

    CONFIRMED(1, "Confirmed");

    companion object {
        private val enumMap: Map<Int, ManualReleaseConfirmationStatus> = entries.associateBy { it.value }

        fun valueOfOrNull(value: Int?): ManualReleaseConfirmationStatus? = enumMap[value]
    }

    @jakarta.persistence.Converter(autoApply = false)
    class Converter : AttributeConverter<ManualReleaseConfirmationStatus, Int> {
        override fun convertToDatabaseColumn(attribute: ManualReleaseConfirmationStatus?): Int? {
            return attribute?.value
        }

        override fun convertToEntityAttribute(dbData: Int?): ManualReleaseConfirmationStatus? {
            return valueOfOrNull(dbData)
        }
    }
}
