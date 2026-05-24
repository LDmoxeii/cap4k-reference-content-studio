package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.factory

import com.only4.cap4k.ddd.core.domain.aggregate.AggregateFactory
import com.only4.cap4k.ddd.core.domain.aggregate.AggregatePayload
import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
@Aggregate(
    aggregate = "Content",
    name = "ContentFactory",
    type = Aggregate.TYPE_FACTORY,
    description = ""
)
class ContentFactory : AggregateFactory<ContentFactory.Payload, Content> {

    override fun create(entityPayload: Payload): Content {
        val title = entityPayload.title.trim()
        val body = entityPayload.body.trim()
        val mediaSourceKey = entityPayload.mediaSourceKey.trim()

        require(title.isNotBlank()) { "Title is required" }
        require(title.length <= TITLE_MAX_LENGTH) { "Title must be 200 characters or fewer" }
        require(body.isNotBlank()) { "Body is required" }
        require(mediaSourceKey.isNotBlank()) { "Media source key is required" }
        require(mediaSourceKey.length <= MEDIA_SOURCE_KEY_MAX_LENGTH) {
            "Media source key must be 200 characters or fewer"
        }

        return Content(
            id = entityPayload.id,
            title = title,
            body = body,
            mediaSourceKey = mediaSourceKey,
            reviewStatus = entityPayload.reviewStatus,
            contentStatus = entityPayload.contentStatus,
            releasePolicy = entityPayload.releasePolicy,
            reviewerId = entityPayload.reviewerId,
            reviewedAt = entityPayload.reviewedAt,
            publishedAt = entityPayload.publishedAt,
            mediaReadyAt = entityPayload.mediaReadyAt,
            dbCreatedAt = entityPayload.dbCreatedAt,
            dbUpdatedAt = entityPayload.dbUpdatedAt,
        )
    }

    private companion object {
        const val TITLE_MAX_LENGTH = 200
        const val MEDIA_SOURCE_KEY_MAX_LENGTH = 200
    }

    @Aggregate(
        aggregate = "Content",
        name = "ContentPayload",
        type = Aggregate.TYPE_FACTORY_PAYLOAD,
        description = ""
    )

    data class Payload(

        val id: ContentId,

        val title: String,

        val body: String,

        val mediaSourceKey: String,

        val reviewStatus: ReviewStatus,

        val contentStatus: ContentStatus,

        val releasePolicy: ReleasePolicy = ReleasePolicy.IMMEDIATE,

        val reviewerId: ReviewerId?,

        val reviewedAt: LocalDateTime?,

        val publishedAt: LocalDateTime?,

        val mediaReadyAt: LocalDateTime? = null,

        val dbCreatedAt: LocalDateTime,

        val dbUpdatedAt: LocalDateTime

    ) : AggregatePayload<Content>

}
