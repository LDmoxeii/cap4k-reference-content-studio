package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.factory

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ContentFactoryTest {

    private val factory = ContentFactory()

    @Test
    fun `create builds content aggregate from payload`() {
        val now = LocalDateTime.of(2026, 5, 9, 9, 0)
        val reviewerId = UUID.randomUUID()
        val mediaReadyAt = now.plusMinutes(30)
        val payload = validPayload(
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

    @Test
    fun `create rejects blank title`() {
        val payload = validPayload(title = "   ")

        assertThrows(IllegalArgumentException::class.java) {
            factory.create(payload)
        }
    }

    @Test
    fun `create rejects title longer than 200 chars`() {
        val payload = validPayload(title = "A".repeat(201))

        assertThrows(IllegalArgumentException::class.java) {
            factory.create(payload)
        }
    }

    @Test
    fun `create rejects blank body`() {
        val payload = validPayload(body = "   ")

        assertThrows(IllegalArgumentException::class.java) {
            factory.create(payload)
        }
    }

    @Test
    fun `create rejects blank media source key`() {
        val payload = validPayload(mediaSourceKey = "   ")

        assertThrows(IllegalArgumentException::class.java) {
            factory.create(payload)
        }
    }

    @Test
    fun `create rejects media source key longer than 200 chars`() {
        val payload = validPayload(mediaSourceKey = "M".repeat(201))

        assertThrows(IllegalArgumentException::class.java) {
            factory.create(payload)
        }
    }

    @Test
    fun `create trims title body and media source key`() {
        val payload = validPayload(
            title = "  Draft title  ",
            body = "  Draft body  ",
            mediaSourceKey = "  media/source-key  ",
        )

        val content = factory.create(payload)

        assertEquals("Draft title", content.title)
        assertEquals("Draft body", content.body)
        assertEquals("media/source-key", content.mediaSourceKey)
    }

    private fun validPayload(
        id: UUID = UUID.randomUUID(),
        title: String = "Draft title",
        body: String = "Draft body",
        mediaSourceKey: String = "media/source-key",
        reviewStatus: ReviewStatus = ReviewStatus.PENDING,
        contentStatus: ContentStatus = ContentStatus.DRAFT,
        reviewerId: UUID? = null,
        reviewedAt: LocalDateTime? = null,
        publishedAt: LocalDateTime? = null,
        mediaReadyAt: LocalDateTime? = null,
        dbCreatedAt: LocalDateTime = LocalDateTime.of(2026, 5, 9, 9, 0),
        dbUpdatedAt: LocalDateTime = dbCreatedAt.plusMinutes(5),
    ): ContentFactory.Payload =
        ContentFactory.Payload(
            id = id,
            title = title,
            body = body,
            mediaSourceKey = mediaSourceKey,
            reviewStatus = reviewStatus,
            contentStatus = contentStatus,
            reviewerId = reviewerId,
            reviewedAt = reviewedAt,
            publishedAt = publishedAt,
            mediaReadyAt = mediaReadyAt,
            dbCreatedAt = dbCreatedAt,
            dbUpdatedAt = dbUpdatedAt,
        )
}
