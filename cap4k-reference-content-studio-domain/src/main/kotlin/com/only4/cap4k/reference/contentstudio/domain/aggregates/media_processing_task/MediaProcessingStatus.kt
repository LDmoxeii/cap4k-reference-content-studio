package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task

enum class MediaProcessingStatus {
    PENDING,
    SUBMITTED,
    SUCCEEDED,
    ;

    companion object {
        fun from(value: String): MediaProcessingStatus = valueOf(value)
    }
}
