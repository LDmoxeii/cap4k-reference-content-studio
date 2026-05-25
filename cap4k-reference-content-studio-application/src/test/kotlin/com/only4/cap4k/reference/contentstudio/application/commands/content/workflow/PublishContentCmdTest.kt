package com.only4.cap4k.reference.contentstudio.application.commands.content.workflow

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PublishContentCmdTest {

    @Test
    fun `decision explains why paid content is not published by generic command`() {
        val decision = PublishContentCmd.decideLoadedContent(
            content(releasePolicy = ReleasePolicy.PAID)
        )

        assertEquals(PublishContentCmd.Decision.NotImmediateContent, decision)
    }

    @Test
    fun `decision explains why immediate content retreats before publication readiness`() {
        val decision = PublishContentCmd.decideLoadedContent(
            content(mediaReadyAt = null)
        )

        assertEquals(PublishContentCmd.Decision.NotPublicationReady, decision)
    }

    @Test
    fun `decision allows immediate content only after approval and media readiness`() {
        val decision = PublishContentCmd.decideLoadedContent(content())

        assertEquals(PublishContentCmd.Decision.Publishable, decision)
    }

    private fun content(
        releasePolicy: ReleasePolicy = ReleasePolicy.IMMEDIATE,
        reviewStatus: ReviewStatus = ReviewStatus.APPROVED,
        contentStatus: ContentStatus = ContentStatus.DRAFT,
        mediaReadyAt: LocalDateTime? = LocalDateTime.of(2026, 5, 17, 10, 0),
    ): Content {
        val now = LocalDateTime.of(2026, 5, 17, 9, 0)
        return Content(
            id = ContentId.new(),
            title = "Publication decision",
            body = "Focused command guard coverage",
            mediaSourceKey = "media/publication-decision.mp4",
            reviewStatus = reviewStatus,
            contentStatus = contentStatus,
            releasePolicy = releasePolicy,
            reviewerId = null,
            reviewedAt = null,
            publishedAt = null,
            mediaReadyAt = mediaReadyAt,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }
}
