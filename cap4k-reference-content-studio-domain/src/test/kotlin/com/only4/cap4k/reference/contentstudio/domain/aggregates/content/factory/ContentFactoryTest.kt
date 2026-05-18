package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.factory

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ContentFactoryTest {

    private val factory = ContentFactory()

    @Test
    fun `create builds content aggregate from payload`() {
        val now = LocalDateTime.of(2026, 5, 9, 9, 0)
        val reviewerId = UUID.randomUUID()
        val mediaReadyAt = now.plusMinutes(30)
        val payload =
            ContentFactory.Payload(
                id = UUID.randomUUID(),
                title = "Draft title",
                body = "Draft body",
                mediaSourceKey = "media/source-key",
                reviewStatus = ReviewStatus.PENDING,
                contentStatus = ContentStatus.DRAFT,
                reviewerId = reviewerId,
                reviewedAt = now,
                publishedAt = now.plusHours(1),
                mediaReadyAt = mediaReadyAt,
                dbCreatedAt = now,
                dbUpdatedAt = now.plusMinutes(5),
            )

        val content = factory.create(payload)

        assertEquals(payload.id, content.id)
        assertEquals(payload.title, content.title)
        assertEquals(payload.body, content.body)
        assertEquals(payload.mediaSourceKey, content.mediaSourceKey)
        assertEquals(payload.reviewStatus, content.reviewStatus)
        assertEquals(payload.contentStatus, content.contentStatus)
        assertEquals(ReleasePolicy.IMMEDIATE, content.releasePolicy)
        assertEquals(payload.reviewerId, content.reviewerId)
        assertEquals(payload.reviewedAt, content.reviewedAt)
        assertEquals(payload.publishedAt, content.publishedAt)
        assertEquals(payload.mediaReadyAt, content.mediaReadyAt)
        assertEquals(payload.dbCreatedAt, content.dbCreatedAt)
        assertEquals(payload.dbUpdatedAt, content.dbUpdatedAt)
    }
}
