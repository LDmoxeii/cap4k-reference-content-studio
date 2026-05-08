package com.only4.cap4k.reference.contentstudio.domain.aggregates.content

import com.only4.cap4k.reference.contentstudio.domain.installTestDomainEventSupervisor
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentPublishedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentReviewApprovedDomainEvent
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
    fun `approve marks review approved and emits content approved event`() {
        val content = newContent()
        val reviewerId = UUID.randomUUID()
        val approvedAt = LocalDateTime.of(2026, 5, 9, 10, 0)

        content.approve(reviewerId = reviewerId, approvedAt = approvedAt)

        assertEquals(ReviewStatus.APPROVED.name, content.reviewStatus)
        assertEquals(reviewerId, content.reviewerId)
        assertEquals(approvedAt, content.reviewedAt)

        val event = assertInstanceOf(
            ContentReviewApprovedDomainEvent::class.java,
            domainEvents.attachedEvents.single(),
        )
        assertEquals(content.id, event.contentId)
        assertEquals(reviewerId, event.reviewerId)
        assertEquals(approvedAt, event.reviewedAt)
    }

    @Test
    fun `publish marks content published and emits content published event`() {
        val content = newContent(reviewStatus = ReviewStatus.APPROVED.name)
        val publishedAt = LocalDateTime.of(2026, 5, 9, 11, 0)

        content.publish(publishedAt = publishedAt)

        assertEquals(ContentStatus.PUBLISHED.name, content.contentStatus)
        assertEquals(publishedAt, content.publishedAt)

        val event = assertInstanceOf(
            ContentPublishedDomainEvent::class.java,
            domainEvents.attachedEvents.single(),
        )
        assertEquals(content.id, event.contentId)
        assertEquals(publishedAt, event.publishedAt)
    }

    private fun newContent(
        reviewStatus: String = ReviewStatus.PENDING.name,
        contentStatus: String = ContentStatus.DRAFT.name,
    ): Content {
        val now = LocalDateTime.of(2026, 5, 9, 9, 0)
        return Content(
            id = UUID.randomUUID(),
            title = "Draft title",
            body = "Draft body",
            mediaSourceKey = "media/source-key",
            reviewStatus = reviewStatus,
            contentStatus = contentStatus,
            reviewerId = null,
            reviewedAt = null,
            publishedAt = null,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }
}
