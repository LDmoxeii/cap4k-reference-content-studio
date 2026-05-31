package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events

import com.only4.cap4k.ddd.core.annotation.BuildingBlock
import com.only4.cap4k.ddd.core.domain.event.annotation.DomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import java.time.LocalDateTime

@DomainEvent(persist = true)
@BuildingBlock(
    tag = "domain_event",
    name = "ContentReviewApproved",
    packageName = "content",
    description = "content review approved",
    aggregates = ["Content"],
    eventName = "",
    family = "domain-event",
    variant = ""
)
class ContentReviewApprovedDomainEvent(
    val entity: Content,
    val contentId: ContentId,
    val reviewerId: ReviewerId,
    val reviewedAt: LocalDateTime,
)
