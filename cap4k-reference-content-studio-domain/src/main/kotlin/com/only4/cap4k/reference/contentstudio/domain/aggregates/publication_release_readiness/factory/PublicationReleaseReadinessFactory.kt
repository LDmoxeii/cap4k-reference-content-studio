package com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.factory

import com.only4.cap4k.ddd.core.domain.aggregate.AggregateFactory
import com.only4.cap4k.ddd.core.domain.aggregate.AggregatePayload
import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.PublicationReleaseReadiness
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.CopyrightReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.ManualReleaseConfirmationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.PublicationReleaseReadinessState
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

@Service
@Aggregate(
    aggregate = "PublicationReleaseReadiness",
    name = "PublicationReleaseReadinessFactory",
    type = Aggregate.TYPE_FACTORY,
    description = ""
)
class PublicationReleaseReadinessFactory :
    AggregateFactory<PublicationReleaseReadinessFactory.Payload, PublicationReleaseReadiness> {

    override fun create(entityPayload: Payload): PublicationReleaseReadiness =
        PublicationReleaseReadiness(
            id = entityPayload.id,
            contentId = entityPayload.contentId,
            mediaProcessingTaskId = entityPayload.mediaProcessingTaskId,
            readinessState = entityPayload.readinessState,
            copyrightStatus = entityPayload.copyrightStatus,
            manualConfirmationStatus = entityPayload.manualConfirmationStatus,
            releaseWindowOpensAt = entityPayload.releaseWindowOpensAt,
            releaseWindowClosesAt = entityPayload.releaseWindowClosesAt,
            readyAt = entityPayload.readyAt,
            cancelReason = entityPayload.cancelReason,
            dbCreatedAt = entityPayload.dbCreatedAt,
            dbUpdatedAt = entityPayload.dbUpdatedAt,
        )

    @Aggregate(
        aggregate = "PublicationReleaseReadiness",
        name = "PublicationReleaseReadinessPayload",
        type = Aggregate.TYPE_FACTORY_PAYLOAD,
        description = ""
    )
    data class Payload(

        val id: UUID,

        val contentId: UUID,

        val mediaProcessingTaskId: UUID,

        val readinessState: PublicationReleaseReadinessState,

        val copyrightStatus: CopyrightReviewStatus,

        val manualConfirmationStatus: ManualReleaseConfirmationStatus,

        val releaseWindowOpensAt: LocalDateTime,

        val releaseWindowClosesAt: LocalDateTime,

        val readyAt: LocalDateTime?,

        val cancelReason: String?,

        val dbCreatedAt: LocalDateTime,

        val dbUpdatedAt: LocalDateTime

    ) : AggregatePayload<PublicationReleaseReadiness>
}
