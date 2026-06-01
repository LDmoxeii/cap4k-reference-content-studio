package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.outbound.content.ContentPublishedIntegrationEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentPublishedDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

/**
 * content published
 */
@Service
@BuildingBlock(
    tag = "domain_event",
    name = "ContentPublished",
    packageName = "content",
    description = "content published",
    aggregates = ["Content"],
    family = "domain-subscriber"
)
class ContentPublishedDomainEventSubscriber {

    @EventListener(ContentPublishedDomainEvent::class)
    fun publishContentIntegrationEvent(event: ContentPublishedDomainEvent) {
        Mediator.events.attach(
            ContentPublishedIntegrationEvent(
                contentId = event.contentId,
                releasePolicy = event.releasePolicy,
                publishedAt = event.publishedAt,
            )
        )
    }
}
