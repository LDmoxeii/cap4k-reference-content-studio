package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import com.only4.cap4k.ddd.core.application.RequestSupervisor
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.StartMediaProcessingCmd
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentReviewApprovedDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * content review approved
 */
@Service
class ContentReviewApprovedDomainEventSubscriber(
    private val requestSupervisor: RequestSupervisor = RequestSupervisor.instance,
) {

    @EventListener(ContentReviewApprovedDomainEvent::class)
    fun on(event: ContentReviewApprovedDomainEvent) {
        requestSupervisor.send(
            StartMediaProcessingCmd.Request(
                contentId = event.contentId,
            )
        )
    }
}
