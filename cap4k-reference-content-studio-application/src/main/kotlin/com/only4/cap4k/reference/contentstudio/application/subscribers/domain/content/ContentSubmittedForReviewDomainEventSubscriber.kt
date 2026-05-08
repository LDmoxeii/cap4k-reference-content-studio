package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentSubmittedForReviewDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * content submitted for review
 */
@Service
class ContentSubmittedForReviewDomainEventSubscriber {

    @EventListener(ContentSubmittedForReviewDomainEvent::class)
    fun on(event: ContentSubmittedForReviewDomainEvent) {
    }
}
