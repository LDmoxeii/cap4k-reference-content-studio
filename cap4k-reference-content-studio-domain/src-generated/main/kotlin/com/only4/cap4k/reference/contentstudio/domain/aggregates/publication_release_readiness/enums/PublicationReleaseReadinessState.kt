package com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums

import jakarta.persistence.AttributeConverter

enum class PublicationReleaseReadinessState(
    val value: Int,
    val description: String
) {

    WAITING(0, "Waiting"),

    READY(1, "Ready"),

    CANCELLED(2, "Cancelled"),

    EXPIRED(3, "Expired");

    companion object {
        private val enumMap: Map<Int, PublicationReleaseReadinessState> = entries.associateBy { it.value }

        fun valueOfOrNull(value: Int?): PublicationReleaseReadinessState? = enumMap[value]
    }

    @jakarta.persistence.Converter(autoApply = false)
    class Converter : AttributeConverter<PublicationReleaseReadinessState, Int> {
        override fun convertToDatabaseColumn(attribute: PublicationReleaseReadinessState?): Int? {
            return attribute?.value
        }

        override fun convertToEntityAttribute(dbData: Int?): PublicationReleaseReadinessState? {
            return valueOfOrNull(dbData)
        }
    }
}
