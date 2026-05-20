package com.only4.cap4k.reference.contentstudio.application.subscribers.integration.outbound.content

import com.only4.cap4k.ddd.core.application.event.annotation.IntegrationEvent
import java.time.LocalDateTime
import java.util.UUID

/**
 * content published integration event
 */
@IntegrationEvent(
    value = "cap4k.reference.contentstudio.content.published",
    subscriber = IntegrationEvent.NONE_SUBSCRIBER
)
data class ContentPublishedIntegrationEvent(
    val contentId: UUID,
    val releasePolicy: String,
    val publishedAt: LocalDateTime
) {
    companion object {
        const val EVENT_NAME = "cap4k.reference.contentstudio.content.published"
    }
}
