package com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.factory

import com.only4.cap4k.ddd.core.domain.aggregate.AggregateFactory
import com.only4.cap4k.ddd.core.domain.aggregate.AggregatePayload
import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
@Aggregate(
    aggregate = "PaidPublicationTask",
    name = "PaidPublicationTaskFactory",
    type = Aggregate.TYPE_FACTORY,
    description = ""
)
class PaidPublicationTaskFactory : AggregateFactory<PaidPublicationTaskFactory.Payload, PaidPublicationTask> {

    override fun create(entityPayload: Payload): PaidPublicationTask =
        PaidPublicationTask(
            id = PaidPublicationTaskId.new(),
            contentId = entityPayload.contentId,
            paidPublicationStatus = PaidPublicationStatus.PENDING,
            publicationSagaId = null,
            payoutHoldStatus = PayoutHoldStatus.NONE,
            payoutHoldId = null,
            entitlementPlanStatus = EntitlementPlanStatus.NONE,
            entitlementPlanId = null,
            startedAt = null,
            publishedAt = null,
            completedAt = null,
            failedAt = null,
            failedReason = null,
            dbCreatedAt = entityPayload.now,
            dbUpdatedAt = entityPayload.now
        )

    @Aggregate(
        aggregate = "PaidPublicationTask",
        name = "PaidPublicationTaskPayload",
        type = Aggregate.TYPE_FACTORY_PAYLOAD,
        description = ""
    )
    data class Payload(
        val contentId: ContentId,
        val now: LocalDateTime
    ) : AggregatePayload<PaidPublicationTask>

}
