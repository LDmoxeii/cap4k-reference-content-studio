package com.only4.cap4k.reference.contentstudio.domain.aggregates.content

enum class ReviewStatus {
    PENDING,
    APPROVED,
    ;

    companion object {
        fun from(value: String): ReviewStatus = valueOf(value)
    }
}
