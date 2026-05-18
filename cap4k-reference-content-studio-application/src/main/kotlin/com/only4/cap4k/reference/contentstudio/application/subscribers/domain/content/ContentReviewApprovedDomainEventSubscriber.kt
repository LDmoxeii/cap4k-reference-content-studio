package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.StartMediaProcessingCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.TryStartPaidPublicationCmd
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentReviewApprovedDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * content review approved
 */
@Service
class ContentReviewApprovedDomainEventSubscriber {

    @EventListener(ContentReviewApprovedDomainEvent::class)
    fun onContentReviewApproved(event: ContentReviewApprovedDomainEvent) {
        Mediator.cmd.send(
            StartMediaProcessingCmd.Request(
                contentId = event.contentId,
            )
        )
    }

    @EventListener(ContentReviewApprovedDomainEvent::class)
    fun tryStartPaidPublication(event: ContentReviewApprovedDomainEvent) {
        Mediator.cmd.send(
            TryStartPaidPublicationCmd.Request(
                contentId = event.contentId,
            )
        )
    }
}
