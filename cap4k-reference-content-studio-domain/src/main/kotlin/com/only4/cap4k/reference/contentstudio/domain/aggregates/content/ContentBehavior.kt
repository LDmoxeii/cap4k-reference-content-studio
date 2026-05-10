package com.only4.cap4k.reference.contentstudio.domain.aggregates.content

import com.only4.cap4k.ddd.core.domain.event.DomainEventSupervisorSupport.events
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentDraftCreatedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentPublishedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentReviewApprovedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentSubmittedForReviewDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import java.time.LocalDateTime
import java.util.UUID

fun Content.approve(reviewerId: UUID, approvedAt: LocalDateTime) {
    if (reviewStatus == ReviewStatus.APPROVED) {
        return
    }

    reviewStatus = ReviewStatus.APPROVED
    this.reviewerId = reviewerId
    reviewedAt = approvedAt
    events().attach(this) {
        ContentReviewApprovedDomainEvent(
            entity = this,
            contentId = id,
            reviewerId = reviewerId,
            reviewedAt = approvedAt,
        )
    }
}

fun Content.onCreate() {
    events().attach(this) {
        ContentDraftCreatedDomainEvent(
            entity = this,
            contentId = id,
            mediaSourceKey = mediaSourceKey,
        )
    }
}

fun Content.submitForReview() {
    check(contentStatus != ContentStatus.PUBLISHED) {
        "Cannot submit published content for review."
    }

    reviewStatus = ReviewStatus.PENDING
    reviewerId = null
    reviewedAt = null
    events().attach(this) {
        ContentSubmittedForReviewDomainEvent(
            entity = this,
            contentId = id,
        )
    }
}

fun Content.publish(publishedAt: LocalDateTime) {
    if (contentStatus == ContentStatus.PUBLISHED) {
        return
    }

    contentStatus = ContentStatus.PUBLISHED
    this.publishedAt = publishedAt
    events().attach(this) {
        ContentPublishedDomainEvent(
            entity = this,
            contentId = id,
            publishedAt = publishedAt,
        )
    }
}
