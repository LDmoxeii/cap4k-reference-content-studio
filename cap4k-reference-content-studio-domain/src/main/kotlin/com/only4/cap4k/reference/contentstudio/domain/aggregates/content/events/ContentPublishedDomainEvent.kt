package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events

import com.only4.cap4k.ddd.core.annotation.BuildingBlock
import com.only4.cap4k.ddd.core.domain.event.annotation.DomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import java.time.LocalDateTime

@DomainEvent(persist = true)
@BuildingBlock(
    tag = "domain_event",
    name = "ContentPublished",
    packageName = "content",
    description = "content published",
    aggregates = ["Content"],
    eventName = "",
    family = "domain-event",
    variant = ""
)
class ContentPublishedDomainEvent(
    val entity: Content,
    val contentId: ContentId,
    val releasePolicy: String,
    val publishedAt: LocalDateTime,
)
