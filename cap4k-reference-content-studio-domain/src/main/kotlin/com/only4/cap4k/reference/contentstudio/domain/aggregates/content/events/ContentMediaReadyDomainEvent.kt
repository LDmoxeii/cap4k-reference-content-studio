package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events

import com.only4.cap4k.ddd.core.annotation.BuildingBlock
import com.only4.cap4k.ddd.core.domain.event.annotation.DomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import java.time.LocalDateTime

/**
 * content media ready
 */
@DomainEvent(persist = true)
@BuildingBlock(
    tag = "domain_event",
    name = "ContentMediaReady",
    packageName = "content",
    description = "content media ready",
    aggregates = ["Content"],
    family = "domain-event"
)
class ContentMediaReadyDomainEvent(
    val entity: Content,
    val contentId: ContentId,
    val mediaReadyAt: LocalDateTime
) {
}
