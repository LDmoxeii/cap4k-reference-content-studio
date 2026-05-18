package com.only4.cap4k.reference.contentstudio.domain.aggregates.content

import com.only4.cap4k.reference.contentstudio.domain.installTestDomainEventSupervisor
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentDraftCreatedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentMediaReadyDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentPublicationReadyDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentPublishedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentReviewApprovedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.MediaProcessingRequestedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class ContentBehaviorTest {
    private lateinit var domainEvents: com.only4.cap4k.reference.contentstudio.domain.TestDomainEventSupervisor

    @BeforeEach
    fun setUp() {
        domainEvents = installTestDomainEventSupervisor()
    }

    @Test
    fun `onCreate emits content draft created event`() {
        val content = newContent()

        content.onCreate()

        val event = assertInstanceOf(
            ContentDraftCreatedDomainEvent::class.java,
            domainEvents.attachedEvents.single(),
        )
        assertEquals(content.id, event.contentId)
        assertEquals(content.mediaSourceKey, event.mediaSourceKey)
    }

    @Test
    fun `approve marks review approved and emits content approved event`() {
        val content = newContent()
        val reviewerId = UUID.randomUUID()
        val approvedAt = LocalDateTime.of(2026, 5, 9, 10, 0)

        content.approve(reviewerId = reviewerId, approvedAt = approvedAt)

        assertEquals(ReviewStatus.APPROVED, content.reviewStatus)
        assertEquals(reviewerId, content.reviewerId)
        assertEquals(approvedAt, content.reviewedAt)

        val reviewApprovedEvent = assertInstanceOf(
            ContentReviewApprovedDomainEvent::class.java,
            domainEvents.attachedEvents[0],
        )
        assertEquals(content.id, reviewApprovedEvent.contentId)
        assertEquals(reviewerId, reviewApprovedEvent.reviewerId)
        assertEquals(approvedAt, reviewApprovedEvent.reviewedAt)
        val mediaProcessingRequestedEvent = assertInstanceOf(
            MediaProcessingRequestedDomainEvent::class.java,
            domainEvents.attachedEvents[1],
        )
        assertEquals(content.id, mediaProcessingRequestedEvent.contentId)
        assertEquals(content.mediaSourceKey, mediaProcessingRequestedEvent.mediaSourceKey)
    }

    @Test
    fun `approve is a no-op when content is already approved`() {
        val originalReviewerId = UUID.randomUUID()
        val originalApprovedAt = LocalDateTime.of(2026, 5, 9, 10, 0)
        val content = newContent(reviewStatus = ReviewStatus.APPROVED).apply {
            reviewerId = originalReviewerId
            reviewedAt = originalApprovedAt
        }

        content.approve(
            reviewerId = UUID.randomUUID(),
            approvedAt = LocalDateTime.of(2026, 5, 9, 11, 0),
        )

        assertEquals(ReviewStatus.APPROVED, content.reviewStatus)
        assertEquals(originalReviewerId, content.reviewerId)
        assertEquals(originalApprovedAt, content.reviewedAt)
        assertEquals(emptyList<Any>(), domainEvents.attachedEvents)
    }

    @Test
    fun `approve emits content publication ready when media was already ready`() {
        val mediaReadyAt = LocalDateTime.of(2026, 5, 9, 9, 30)
        val content = newContent(mediaReadyAt = mediaReadyAt)
        val reviewerId = UUID.randomUUID()
        val approvedAt = LocalDateTime.of(2026, 5, 9, 10, 0)

        content.approve(reviewerId = reviewerId, approvedAt = approvedAt)

        assertEquals(2, domainEvents.attachedEvents.size)
        val readyEvent = assertInstanceOf(
            ContentPublicationReadyDomainEvent::class.java,
            domainEvents.attachedEvents[1],
        )
        assertEquals(content.id, readyEvent.contentId)
    }

    @Test
    fun `record media ready emits content media ready and publication ready when review is approved`() {
        val content = newContent(reviewStatus = ReviewStatus.APPROVED)
        val mediaReadyAt = LocalDateTime.of(2026, 5, 9, 10, 30)

        content.recordMediaReady(mediaReadyAt)

        assertEquals(mediaReadyAt, content.mediaReadyAt)
        val mediaReadyEvent = assertInstanceOf(
            ContentMediaReadyDomainEvent::class.java,
            domainEvents.attachedEvents[0],
        )
        assertEquals(content.id, mediaReadyEvent.contentId)
        assertEquals(mediaReadyAt, mediaReadyEvent.mediaReadyAt)
        val readyEvent = assertInstanceOf(
            ContentPublicationReadyDomainEvent::class.java,
            domainEvents.attachedEvents[1],
        )
        assertEquals(content.id, readyEvent.contentId)
    }

    @Test
    fun `publish marks content published and emits content published event`() {
        val content = newContent(
            reviewStatus = ReviewStatus.APPROVED,
            mediaReadyAt = LocalDateTime.of(2026, 5, 9, 10, 30),
        )
        val publishedAt = LocalDateTime.of(2026, 5, 9, 11, 0)

        content.publish(publishedAt = publishedAt)

        assertEquals(ContentStatus.PUBLISHED, content.contentStatus)
        assertEquals(publishedAt, content.publishedAt)

        val event = assertInstanceOf(
            ContentPublishedDomainEvent::class.java,
            domainEvents.attachedEvents.single(),
        )
        assertEquals(content.id, event.contentId)
        assertEquals(publishedAt, event.publishedAt)
    }

    @Test
    fun `publish is a no-op when content is already published`() {
        val originalPublishedAt = LocalDateTime.of(2026, 5, 9, 11, 0)
        val content = newContent(
            reviewStatus = ReviewStatus.APPROVED,
            contentStatus = ContentStatus.PUBLISHED,
            mediaReadyAt = LocalDateTime.of(2026, 5, 9, 10, 30),
        ).apply {
            publishedAt = originalPublishedAt
        }

        content.publish(publishedAt = LocalDateTime.of(2026, 5, 9, 12, 0))

        assertEquals(ContentStatus.PUBLISHED, content.contentStatus)
        assertEquals(originalPublishedAt, content.publishedAt)
        assertEquals(emptyList<Any>(), domainEvents.attachedEvents)
    }

    private fun newContent(
        reviewStatus: ReviewStatus = ReviewStatus.PENDING,
        contentStatus: ContentStatus = ContentStatus.DRAFT,
        mediaReadyAt: LocalDateTime? = null,
    ): Content {
        val now = LocalDateTime.of(2026, 5, 9, 9, 0)
        return Content(
            id = UUID.randomUUID(),
            title = "Draft title",
            body = "Draft body",
            mediaSourceKey = "media/source-key",
            reviewStatus = reviewStatus,
            contentStatus = contentStatus,
            releasePolicy = ReleasePolicy.IMMEDIATE,
            reviewerId = null,
            reviewedAt = null,
            publishedAt = null,
            mediaReadyAt = mediaReadyAt,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }
}
