package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events

import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.ddd.core.domain.event.annotation.DomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import java.time.LocalDateTime
import java.util.UUID

@DomainEvent(persist = true)
@Aggregate(
    aggregate = "Content",
    name = "ContentReviewApprovedDomainEvent",
    type = Aggregate.TYPE_DOMAIN_EVENT,
    description = "",
)
class ContentReviewApprovedDomainEvent(
    val entity: Content,
    val contentId: UUID,
    val reviewerId: UUID,
    val reviewedAt: LocalDateTime,
)
