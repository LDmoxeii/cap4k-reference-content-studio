package com.only4.cap4k.reference.contentstudio.domain.aggregates.content

import com.only4.cap4k.ddd.core.domain.event.DomainEventSupervisorSupport.events
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentDraftCreatedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentMediaReadyDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentPublicationReadyDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentPublishedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentRequiresMediaProcessingDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentReviewApprovedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentSubmittedForReviewDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import java.time.LocalDateTime
import java.util.UUID

fun Content.approve(reviewerId: UUID, approvedAt: LocalDateTime) {
    if (reviewStatus == ReviewStatus.APPROVED) {
        return
    }

    val wasPublicationReady = isPublicationReady()
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
    if (mediaReadyAt == null) {
        events().attach(this) {
            ContentRequiresMediaProcessingDomainEvent(
                entity = this,
                contentId = id,
                mediaSourceKey = mediaSourceKey,
            )
        }
    }
    attachPublicationReadyIfNeeded(wasPublicationReady)
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

    check(reviewStatus == ReviewStatus.APPROVED) {
        "Cannot publish content before review is approved."
    }
    check(mediaReadyAt != null) {
        "Cannot publish content before media is ready."
    }

    contentStatus = ContentStatus.PUBLISHED
    this.publishedAt = publishedAt
    events().attach(this) {
        ContentPublishedDomainEvent(
            entity = this,
            contentId = id,
            releasePolicy = releasePolicy.name,
            publishedAt = publishedAt,
        )
    }
}

fun Content.recordMediaReady(mediaReadyAt: LocalDateTime) {
    val wasPublicationReady = isPublicationReady()
    this.mediaReadyAt?.let {
        return
    }

    this.mediaReadyAt = mediaReadyAt
    events().attach(this) {
        ContentMediaReadyDomainEvent(
            entity = this,
            contentId = id,
            mediaReadyAt = mediaReadyAt,
        )
    }
    attachPublicationReadyIfNeeded(wasPublicationReady)
}

fun Content.isReadyForImmediatePublication(): Boolean =
    releasePolicy == ReleasePolicy.IMMEDIATE && isPublicationReady()

fun Content.isReadyForPaidPublication(): Boolean =
    releasePolicy == ReleasePolicy.PAID && isPublicationReady()

private fun Content.isPublicationReady(): Boolean =
    reviewStatus == ReviewStatus.APPROVED &&
        mediaReadyAt != null &&
        contentStatus != ContentStatus.PUBLISHED

private fun Content.attachPublicationReadyIfNeeded(wasPublicationReady: Boolean) {
    if (wasPublicationReady || !isPublicationReady()) {
        return
    }

    events().attach(this) {
        ContentPublicationReadyDomainEvent(
            entity = this,
            contentId = id,
        )
    }
}
