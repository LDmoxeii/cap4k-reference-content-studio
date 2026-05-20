package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events

import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.ddd.core.domain.event.annotation.DomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import java.util.UUID

/**
 * content requires media processing
 */
@DomainEvent(persist = true)
@Aggregate(
    aggregate = "Content",
    name = "ContentRequiresMediaProcessingDomainEvent",
    type = Aggregate.TYPE_DOMAIN_EVENT,
    description = "content requires media processing"
)
class ContentRequiresMediaProcessingDomainEvent(
    val entity: Content,
    val contentId: UUID,
    val mediaSourceKey: String
) {
}
