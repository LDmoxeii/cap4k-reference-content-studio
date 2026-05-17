package com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.events

import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.ddd.core.domain.event.annotation.DomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.PublicationReleaseReadiness
import java.util.UUID

/**
 * manual release confirmed for publication release readiness
 */
@DomainEvent(persist = true)
@Aggregate(
    aggregate = "PublicationReleaseReadiness",
    name = "ManualReleaseConfirmedDomainEvent",
    type = Aggregate.TYPE_DOMAIN_EVENT,
    description = "manual release confirmed for publication release readiness"
)
class ManualReleaseConfirmedDomainEvent(
    val entity: PublicationReleaseReadiness,
    val contentId: UUID
) {
}
