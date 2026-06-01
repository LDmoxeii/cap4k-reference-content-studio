package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.StartMediaProcessingCmd
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentRequiresMediaProcessingDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

/**
 * content requires media processing
 */
@Service
@BuildingBlock(
    tag = "domain_event",
    name = "ContentRequiresMediaProcessing",
    packageName = "content",
    description = "content requires media processing",
    aggregates = ["Content"],
    family = "domain-subscriber"
)
class ContentRequiresMediaProcessingDomainEventSubscriber {

    @EventListener(ContentRequiresMediaProcessingDomainEvent::class)
    fun startMediaProcessing(event: ContentRequiresMediaProcessingDomainEvent) {
        Mediator.cmd.send(
            StartMediaProcessingCmd.Request(
                contentId = event.contentId,
                mediaSourceKey = event.mediaSourceKey,
            )
        )
    }
}
