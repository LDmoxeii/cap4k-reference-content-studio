package com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness

import com.only4.cap4k.ddd.core.domain.event.DomainEventSupervisorSupport.events
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.CopyrightReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.ManualReleaseConfirmationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.PublicationReleaseReadinessState
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.events.CopyrightReviewPassedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.events.ManualReleaseConfirmedDomainEvent
import java.time.LocalDateTime

fun PublicationReleaseReadiness.passCopyrightReview() {
    if (copyrightStatus == CopyrightReviewStatus.PASSED) {
        return
    }
    check(readinessState == PublicationReleaseReadinessState.WAITING) {
        "Only waiting publication release readiness can pass copyright review."
    }

    copyrightStatus = CopyrightReviewStatus.PASSED
    events().attach(this) {
        CopyrightReviewPassedDomainEvent(
            entity = this,
            contentId = contentId,
        )
    }
}

fun PublicationReleaseReadiness.confirmManualRelease() {
    if (manualConfirmationStatus == ManualReleaseConfirmationStatus.CONFIRMED) {
        return
    }
    check(readinessState == PublicationReleaseReadinessState.WAITING) {
        "Only waiting publication release readiness can confirm manual release."
    }

    manualConfirmationStatus = ManualReleaseConfirmationStatus.CONFIRMED
    events().attach(this) {
        ManualReleaseConfirmedDomainEvent(
            entity = this,
            contentId = contentId,
        )
    }
}

fun PublicationReleaseReadiness.recordPublicationReleaseSagaStarted(sagaId: String, now: LocalDateTime) {
    check(sagaId.isNotBlank()) {
        "Release saga id must not be blank."
    }
    check(canStartPublicationReleaseSaga(now) || releaseSagaId == sagaId) {
        "Publication release readiness cannot start release saga."
    }

    releaseSagaId = sagaId
    dbUpdatedAt = now
}

fun PublicationReleaseReadiness.canStartPublicationReleaseSaga(now: LocalDateTime): Boolean =
    readinessState == PublicationReleaseReadinessState.WAITING &&
        copyrightStatus == CopyrightReviewStatus.PASSED &&
        manualConfirmationStatus == ManualReleaseConfirmationStatus.CONFIRMED &&
        !now.isBefore(releaseWindowOpensAt) &&
        !now.isAfter(releaseWindowClosesAt) &&
        releaseSagaId.isNullOrBlank()

fun PublicationReleaseReadiness.complete(now: LocalDateTime) {
    check(readinessState == PublicationReleaseReadinessState.WAITING) {
        "Only waiting publication release readiness can be completed."
    }
    check(copyrightStatus == CopyrightReviewStatus.PASSED) {
        "Copyright review must be passed before release readiness can complete."
    }
    check(manualConfirmationStatus == ManualReleaseConfirmationStatus.CONFIRMED) {
        "Manual release must be confirmed before release readiness can complete."
    }
    check(!now.isBefore(releaseWindowOpensAt)) {
        "Release readiness cannot complete before release window opens."
    }
    check(!now.isAfter(releaseWindowClosesAt)) {
        "Release readiness cannot complete after release window closes."
    }

    readinessState = PublicationReleaseReadinessState.READY
    readyAt = now
    dbUpdatedAt = now
}
