package com.only4.cap4k.reference.contentstudio.application.subscribers.integration.outbound.content

import com.only4.cap4k.ddd.core.application.event.annotation.IntegrationEvent
import java.time.LocalDateTime
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId

/**
 * content published integration event
 */
@IntegrationEvent(
    value = "cap4k.reference.contentstudio.content.published",
    subscriber = IntegrationEvent.NONE_SUBSCRIBER
)
data class ContentPublishedIntegrationEvent(
    val contentId: ContentId,
    val releasePolicy: String,
    val publishedAt: LocalDateTime
) {
    companion object {
        const val EVENT_NAME = "cap4k.reference.contentstudio.content.published"
    }
}
