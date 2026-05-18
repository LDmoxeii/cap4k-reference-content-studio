package com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task

import com.only4.cap4k.ddd.core.domain.id.ApplicationSideId
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "paid_publication_task")
class PaidPublicationTask(
    id: UUID = UUID(0L, 0L),
    contentId: UUID,
    paidPublicationStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus,
    publicationSagaId: String? = null,
    payoutHoldStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus,
    payoutHoldId: String? = null,
    entitlementPlanStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus,
    entitlementPlanId: String? = null,
    startedAt: java.time.LocalDateTime? = null,
    publishedAt: java.time.LocalDateTime? = null,
    completedAt: java.time.LocalDateTime? = null,
    failedAt: java.time.LocalDateTime? = null,
    failedReason: String? = null,
    dbCreatedAt: java.time.LocalDateTime,
    dbUpdatedAt: java.time.LocalDateTime
) {

    @Id
    @field:ApplicationSideId(strategy = "uuid7")
    @Column(name = "id", insertable = true, updatable = false)
    var id: UUID = id
        internal set

    @Column(name = "content_id")
    var contentId: UUID = contentId
        internal set

    @Column(name = "paid_publication_status")
    @Convert(converter = com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus.Converter::class)
    var paidPublicationStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus = paidPublicationStatus
        internal set

    @Column(name = "publication_saga_id")
    var publicationSagaId: String? = publicationSagaId
        internal set

    @Column(name = "payout_hold_status")
    @Convert(converter = com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus.Converter::class)
    var payoutHoldStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus = payoutHoldStatus
        internal set

    @Column(name = "payout_hold_id")
    var payoutHoldId: String? = payoutHoldId
        internal set

    @Column(name = "entitlement_plan_status")
    @Convert(converter = com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus.Converter::class)
    var entitlementPlanStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus = entitlementPlanStatus
        internal set

    @Column(name = "entitlement_plan_id")
    var entitlementPlanId: String? = entitlementPlanId
        internal set

    @Column(name = "started_at")
    var startedAt: java.time.LocalDateTime? = startedAt
        internal set

    @Column(name = "published_at")
    var publishedAt: java.time.LocalDateTime? = publishedAt
        internal set

    @Column(name = "completed_at")
    var completedAt: java.time.LocalDateTime? = completedAt
        internal set

    @Column(name = "failed_at")
    var failedAt: java.time.LocalDateTime? = failedAt
        internal set

    @Column(name = "failed_reason")
    var failedReason: String? = failedReason
        internal set

    @Column(name = "db_created_at")
    var dbCreatedAt: java.time.LocalDateTime = dbCreatedAt
        internal set

    @Column(name = "db_updated_at")
    var dbUpdatedAt: java.time.LocalDateTime = dbUpdatedAt
        internal set

}
