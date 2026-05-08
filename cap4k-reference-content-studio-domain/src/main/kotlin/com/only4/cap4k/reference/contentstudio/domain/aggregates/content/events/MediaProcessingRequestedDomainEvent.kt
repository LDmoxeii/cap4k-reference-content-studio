package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events

import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.ddd.core.domain.event.annotation.DomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import java.util.UUID

/**
 * media processing requested
 */
@DomainEvent(persist = true)
@Aggregate(
    aggregate = "Content",
    name = "MediaProcessingRequestedDomainEvent",
    type = Aggregate.TYPE_DOMAIN_EVENT,
    description = "media processing requested"
)
class MediaProcessingRequestedDomainEvent(
    val entity: Content,
    val contentId: UUID,
    val mediaSourceKey: String
) {
}
