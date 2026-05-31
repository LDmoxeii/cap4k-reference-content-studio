package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events

import com.only4.cap4k.ddd.core.annotation.BuildingBlock
import com.only4.cap4k.ddd.core.domain.event.annotation.DomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId

/**
 * content requires media processing
 */
@DomainEvent(persist = true)
@BuildingBlock(
    tag = "domain_event",
    name = "ContentRequiresMediaProcessing",
    packageName = "content",
    description = "content requires media processing",
    aggregates = ["Content"],
    eventName = "",
    family = "domain-event",
    variant = ""
)
class ContentRequiresMediaProcessingDomainEvent(
    val entity: Content,
    val contentId: ContentId,
    val mediaSourceKey: String
) {
}
