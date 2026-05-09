package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.factory

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ReviewStatus
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
        val payload =
            ContentFactory.Payload(
                id = UUID.randomUUID(),
                title = "Draft title",
                body = "Draft body",
                mediaSourceKey = "media/source-key",
                reviewStatus = ReviewStatus.PENDING.name,
                contentStatus = ContentStatus.DRAFT.name,
                reviewerId = reviewerId,
                reviewedAt = now,
                publishedAt = now.plusHours(1),
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
        assertEquals(payload.reviewerId, content.reviewerId)
        assertEquals(payload.reviewedAt, content.reviewedAt)
        assertEquals(payload.publishedAt, content.publishedAt)
        assertEquals(payload.dbCreatedAt, content.dbCreatedAt)
        assertEquals(payload.dbUpdatedAt, content.dbUpdatedAt)
    }
}
