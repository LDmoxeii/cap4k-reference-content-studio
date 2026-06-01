package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentSubmittedForReviewDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

/**
 * content submitted for review
 */
@Service
@BuildingBlock(
    tag = "domain_event",
    name = "ContentSubmittedForReview",
    packageName = "content",
    description = "content submitted for review",
    aggregates = ["Content"],
    family = "domain-subscriber"
)
class ContentSubmittedForReviewDomainEventSubscriber {

    @EventListener(ContentSubmittedForReviewDomainEvent::class)
    fun observeReviewSubmission(event: ContentSubmittedForReviewDomainEvent) {
    }
}
