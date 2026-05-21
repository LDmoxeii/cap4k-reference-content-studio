package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentMediaReadyDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * content media ready
 */
@Service
class ContentMediaReadyDomainEventSubscriber {

    @EventListener(ContentMediaReadyDomainEvent::class)
    fun observeMediaReadiness(event: ContentMediaReadyDomainEvent) {
    }
}
