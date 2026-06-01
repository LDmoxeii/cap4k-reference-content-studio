package com.only4.cap4k.reference.contentstudio.application.subscribers.integration.inbound.media.processing

import com.only4.cap4k.ddd.core.application.event.annotation.IntegrationEvent
import java.time.LocalDateTime
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

/**
 * media processing callback from external media service
 */
@IntegrationEvent(
    value = "cap4k.reference.contentstudio.media-processing.completed",
    subscriber = "\${spring.application.name:}"
)
@BuildingBlock(
    tag = "integration_event",
    name = "MediaProcessingCallback",
    packageName = "media.processing",
    description = "media processing callback from external media service",
    aggregates = ["MediaProcessingTask"],
    family = "integration-event",
    variant = "inbound"
)
data class MediaProcessingCallbackIntegrationEvent(
    val externalTaskId: String,
    val status: String,
    val assetSha256: String?,
    val assetLocation: String?,
    val completedAt: LocalDateTime
) {
    companion object {
        const val EVENT_NAME = "cap4k.reference.contentstudio.media-processing.completed"
    }
}
