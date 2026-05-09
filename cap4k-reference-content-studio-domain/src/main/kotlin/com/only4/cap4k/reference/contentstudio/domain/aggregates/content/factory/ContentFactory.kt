package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.factory

import com.only4.cap4k.ddd.core.domain.aggregate.AggregateFactory
import com.only4.cap4k.ddd.core.domain.aggregate.AggregatePayload
import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import org.springframework.stereotype.Service

@Service
@Aggregate(
    aggregate = "Content",
    name = "ContentFactory",
    type = Aggregate.TYPE_FACTORY,
    description = ""
)
class ContentFactory : AggregateFactory<ContentFactory.Payload, Content> {

    override fun create(payload: Payload): Content {
        TODO("Implement aggregate construction")
    }

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

        val reviewStatus: String,

        val contentStatus: String,

        val reviewerId: UUID?,

        val reviewedAt: java.time.LocalDateTime?,

        val publishedAt: java.time.LocalDateTime?,

        val dbCreatedAt: java.time.LocalDateTime,

        val dbUpdatedAt: java.time.LocalDateTime

    ) : AggregatePayload<Content>

}
