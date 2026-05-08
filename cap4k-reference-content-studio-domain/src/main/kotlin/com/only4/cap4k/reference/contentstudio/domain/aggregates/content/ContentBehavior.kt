package com.only4.cap4k.reference.contentstudio.domain.aggregates.content

import com.only4.cap4k.ddd.core.domain.event.DomainEventSupervisorSupport.events
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentDraftCreatedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentPublishedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentReviewApprovedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentSubmittedForReviewDomainEvent
import java.time.LocalDateTime
import java.util.UUID

var Content.reviewStatusValue: ReviewStatus
    get() = ReviewStatus.from(reviewStatus)
    internal set(value) {
        reviewStatus = value.name
    }

var Content.contentStatusValue: ContentStatus
    get() = ContentStatus.from(contentStatus)
    internal set(value) {
        contentStatus = value.name
    }

fun Content.approve(reviewerId: UUID, approvedAt: LocalDateTime) {
    if (reviewStatusValue == ReviewStatus.APPROVED) {
        return
    }

    reviewStatusValue = ReviewStatus.APPROVED
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

fun Content.recordDraftCreated() {
    events().attach(this) {
        ContentDraftCreatedDomainEvent(
            entity = this,
            contentId = id,
            mediaSourceKey = mediaSourceKey,
        )
    }
}

fun Content.submitForReview() {
    check(contentStatusValue != ContentStatus.PUBLISHED) {
        "Cannot submit published content for review."
    }

    reviewStatusValue = ReviewStatus.PENDING
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
    if (contentStatusValue == ContentStatus.PUBLISHED) {
        return
    }

    contentStatusValue = ContentStatus.PUBLISHED
    this.publishedAt = publishedAt
    events().attach(this) {
        ContentPublishedDomainEvent(
            entity = this,
            contentId = id,
            publishedAt = publishedAt,
        )
    }
}
