package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.publication_release_readiness

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.release.readiness.TryContinuePublicationReleaseCmd
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.events.ManualReleaseConfirmedDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * manual release confirmed for publication release readiness
 */
@Service
class ManualReleaseConfirmedDomainEventSubscriber {

    @EventListener(ManualReleaseConfirmedDomainEvent::class)
    fun tryContinuePublicationRelease(event: ManualReleaseConfirmedDomainEvent) {
        Mediator.cmd.send(
            TryContinuePublicationReleaseCmd.Request(
                contentId = event.contentId,
            )
        )
    }
}
