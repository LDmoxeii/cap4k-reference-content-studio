package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.outbound.content.ContentPublishedIntegrationEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentPublishedDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * content published
 */
@Service
class ContentPublishedDomainEventSubscriber {

    @EventListener(ContentPublishedDomainEvent::class)
    fun on(event: ContentPublishedDomainEvent) {
        Mediator.events.attach(
            ContentPublishedIntegrationEvent(
                contentId = event.contentId,
                releasePolicy = event.releasePolicy,
                publishedAt = event.publishedAt,
            )
        )
    }
}
