package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.factory

import com.only4.cap4k.ddd.core.domain.aggregate.AggregateFactory
import com.only4.cap4k.ddd.core.domain.aggregate.AggregatePayload
import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

@Service
@Aggregate(
    aggregate = "Content",
    name = "ContentFactory",
    type = Aggregate.TYPE_FACTORY,
    description = ""
)
class ContentFactory : AggregateFactory<ContentFactory.Payload, Content> {

    override fun create(entityPayload: Payload): Content =
        Content(
            id = entityPayload.id,
            title = entityPayload.title,
            body = entityPayload.body,
            mediaSourceKey = entityPayload.mediaSourceKey,
            reviewStatus = entityPayload.reviewStatus,
            contentStatus = entityPayload.contentStatus,
            releasePolicy = entityPayload.releasePolicy,
            releaseWindowOpensAt = entityPayload.releaseWindowOpensAt,
            releaseWindowClosesAt = entityPayload.releaseWindowClosesAt,
            reviewerId = entityPayload.reviewerId,
            reviewedAt = entityPayload.reviewedAt,
            publishedAt = entityPayload.publishedAt,
            dbCreatedAt = entityPayload.dbCreatedAt,
            dbUpdatedAt = entityPayload.dbUpdatedAt,
        )

    @Aggregate(
        aggregate = "Content",
        name = "ContentPayload",
        type = Aggregate.TYPE_FACTORY_PAYLOAD,
        description = ""
    )

    data class Payload(

        val id: UUID,

        val title: String,

        val body: String,

        val mediaSourceKey: String,

        val reviewStatus: ReviewStatus,

        val contentStatus: ContentStatus,

        val releasePolicy: ReleasePolicy = ReleasePolicy.IMMEDIATE,

        val releaseWindowOpensAt: LocalDateTime? = null,

        val releaseWindowClosesAt: LocalDateTime? = null,

        val reviewerId: UUID?,

        val reviewedAt: LocalDateTime?,

        val publishedAt: LocalDateTime?,

        val dbCreatedAt: LocalDateTime,

        val dbUpdatedAt: LocalDateTime

    ) : AggregatePayload<Content>

}
