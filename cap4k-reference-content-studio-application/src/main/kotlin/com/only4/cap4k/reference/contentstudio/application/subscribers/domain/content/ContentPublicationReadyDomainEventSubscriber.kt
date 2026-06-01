package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.PublishContentCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.TryStartPaidPublicationCmd
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentPublicationReadyDomainEvent
import java.time.LocalDateTime
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

/**
 * content publication ready
 */
@Service
@BuildingBlock(
    tag = "domain_event",
    name = "ContentPublicationReady",
    packageName = "content",
    description = "content publication ready",
    aggregates = ["Content"],
    family = "domain-subscriber"
)
class ContentPublicationReadyDomainEventSubscriber {

    @EventListener(ContentPublicationReadyDomainEvent::class)
    fun continueImmediatePublication(event: ContentPublicationReadyDomainEvent) {
        Mediator.cmd.send(
            PublishContentCmd.Request(
                contentId = event.contentId,
                publishedAt = LocalDateTime.now(),
            )
        )
    }

    @EventListener(ContentPublicationReadyDomainEvent::class)
    fun continuePaidPublication(event: ContentPublicationReadyDomainEvent) {
        Mediator.cmd.send(
            TryStartPaidPublicationCmd.Request(
                contentId = event.contentId,
            )
        )
    }
}
