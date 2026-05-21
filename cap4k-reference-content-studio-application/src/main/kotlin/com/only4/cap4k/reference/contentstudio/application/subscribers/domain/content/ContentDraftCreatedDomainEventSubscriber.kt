package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentDraftCreatedDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * content draft created
 */
@Service
class ContentDraftCreatedDomainEventSubscriber {

    @EventListener(ContentDraftCreatedDomainEvent::class)
    fun observeDraftCreation(event: ContentDraftCreatedDomainEvent) {
    }
}
