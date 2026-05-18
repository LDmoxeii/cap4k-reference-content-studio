package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events

import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.ddd.core.domain.event.annotation.DomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import java.time.LocalDateTime
import java.util.UUID

/**
 * content media ready
 */
@DomainEvent(persist = true)
@Aggregate(
    aggregate = "Content",
    name = "ContentMediaReadyDomainEvent",
    type = Aggregate.TYPE_DOMAIN_EVENT,
    description = "content media ready"
)
class ContentMediaReadyDomainEvent(
    val entity: Content,
    val contentId: UUID,
    val mediaReadyAt: LocalDateTime
) {
}
