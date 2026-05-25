package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events

import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.ddd.core.domain.event.annotation.DomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import java.time.LocalDateTime

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
    val contentId: ContentId,
    val mediaReadyAt: LocalDateTime
) {
}
