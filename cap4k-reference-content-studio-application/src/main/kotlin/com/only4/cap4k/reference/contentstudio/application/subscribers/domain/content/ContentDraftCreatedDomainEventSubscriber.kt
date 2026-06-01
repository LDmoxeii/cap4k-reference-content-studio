package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentDraftCreatedDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

/**
 * content draft created
 */
@Service
@BuildingBlock(
    tag = "domain_event",
    name = "ContentDraftCreated",
    packageName = "content",
    description = "content draft created",
    aggregates = ["Content"],
    family = "domain-subscriber"
)
class ContentDraftCreatedDomainEventSubscriber {

    @EventListener(ContentDraftCreatedDomainEvent::class)
    fun observeDraftCreation(event: ContentDraftCreatedDomainEvent) {
    }
}
