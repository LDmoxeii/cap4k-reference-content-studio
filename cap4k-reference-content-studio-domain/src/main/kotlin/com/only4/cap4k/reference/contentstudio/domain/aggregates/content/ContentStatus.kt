package com.only4.cap4k.reference.contentstudio.domain.aggregates.content

enum class ContentStatus {
    DRAFT,
    PUBLISHED,
    ;

    companion object {
        fun from(value: String): ContentStatus = valueOf(value)
    }
}
