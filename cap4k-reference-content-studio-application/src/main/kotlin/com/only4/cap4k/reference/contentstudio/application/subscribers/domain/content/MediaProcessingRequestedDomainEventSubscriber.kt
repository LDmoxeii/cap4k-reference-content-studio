package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.StartMediaProcessingCmd
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.MediaProcessingRequestedDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * media processing requested
 */
@Service
class MediaProcessingRequestedDomainEventSubscriber {

    @EventListener(MediaProcessingRequestedDomainEvent::class)
    fun on(event: MediaProcessingRequestedDomainEvent) {
        Mediator.cmd.send(
            StartMediaProcessingCmd.Request(
                contentId = event.contentId,
                mediaSourceKey = event.mediaSourceKey,
            )
        )
    }
}
