package com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness

import com.only4.cap4k.ddd.core.domain.id.ApplicationSideId
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "publication_release_readiness")
class PublicationReleaseReadiness(
    id: UUID = UUID(0L, 0L),
    contentId: UUID,
    mediaProcessingTaskId: UUID,
    readinessState: com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.PublicationReleaseReadinessState,
    copyrightStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.CopyrightReviewStatus,
    manualConfirmationStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.ManualReleaseConfirmationStatus,
    releaseWindowOpensAt: java.time.LocalDateTime,
    releaseWindowClosesAt: java.time.LocalDateTime,
    readyAt: java.time.LocalDateTime? = null,
    cancelReason: String? = null,
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

    @Column(name = "media_processing_task_id")
    var mediaProcessingTaskId: UUID = mediaProcessingTaskId
        internal set

    @Column(name = "readiness_state")
    @Convert(converter = com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.PublicationReleaseReadinessState.Converter::class)
    var readinessState: com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.PublicationReleaseReadinessState = readinessState
        internal set

    @Column(name = "copyright_status")
    @Convert(converter = com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.CopyrightReviewStatus.Converter::class)
    var copyrightStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.CopyrightReviewStatus = copyrightStatus
        internal set

    @Column(name = "manual_confirmation_status")
    @Convert(converter = com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.ManualReleaseConfirmationStatus.Converter::class)
    var manualConfirmationStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.ManualReleaseConfirmationStatus = manualConfirmationStatus
        internal set

    @Column(name = "release_window_opens_at")
    var releaseWindowOpensAt: java.time.LocalDateTime = releaseWindowOpensAt
        internal set

    @Column(name = "release_window_closes_at")
    var releaseWindowClosesAt: java.time.LocalDateTime = releaseWindowClosesAt
        internal set

    @Column(name = "ready_at")
    var readyAt: java.time.LocalDateTime? = readyAt
        internal set

    @Column(name = "cancel_reason")
    var cancelReason: String? = cancelReason
        internal set

    @Column(name = "db_created_at")
    var dbCreatedAt: java.time.LocalDateTime = dbCreatedAt
        internal set

    @Column(name = "db_updated_at")
    var dbUpdatedAt: java.time.LocalDateTime = dbUpdatedAt
        internal set

}
