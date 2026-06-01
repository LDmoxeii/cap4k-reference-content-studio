package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentMediaReadyDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

/**
 * content media ready
 */
@Service
@BuildingBlock(
    tag = "domain_event",
    name = "ContentMediaReady",
    packageName = "content",
    description = "content media ready",
    aggregates = ["Content"],
    family = "domain-subscriber"
)
class ContentMediaReadyDomainEventSubscriber {

    @EventListener(ContentMediaReadyDomainEvent::class)
    fun observeMediaReadiness(event: ContentMediaReadyDomainEvent) {
    }
}
