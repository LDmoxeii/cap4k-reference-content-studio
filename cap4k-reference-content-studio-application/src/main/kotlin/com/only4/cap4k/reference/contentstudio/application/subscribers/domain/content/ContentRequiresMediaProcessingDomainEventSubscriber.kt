package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.StartMediaProcessingCmd
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentRequiresMediaProcessingDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * content requires media processing
 */
@Service
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
