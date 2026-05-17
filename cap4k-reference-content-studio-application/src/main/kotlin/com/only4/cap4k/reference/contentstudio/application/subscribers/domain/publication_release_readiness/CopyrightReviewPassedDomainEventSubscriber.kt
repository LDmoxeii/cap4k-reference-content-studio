package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.publication_release_readiness

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.release.readiness.TryContinuePublicationReleaseCmd
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.events.CopyrightReviewPassedDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * copyright review passed for publication release readiness
 */
@Service
class CopyrightReviewPassedDomainEventSubscriber {

    @EventListener(CopyrightReviewPassedDomainEvent::class)
    fun tryContinuePublicationRelease(event: CopyrightReviewPassedDomainEvent) {
        Mediator.cmd.send(
            TryContinuePublicationReleaseCmd.Request(
                contentId = event.contentId,
            )
        )
    }
}
