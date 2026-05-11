package com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness

import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.CopyrightReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.ManualReleaseConfirmationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.PublicationReleaseReadinessState
import java.time.LocalDateTime

fun PublicationReleaseReadiness.passCopyrightReview() {
    check(readinessState == PublicationReleaseReadinessState.WAITING) {
        "Only waiting publication release readiness can pass copyright review."
    }

    copyrightStatus = CopyrightReviewStatus.PASSED
}

fun PublicationReleaseReadiness.confirmManualRelease() {
    check(readinessState == PublicationReleaseReadinessState.WAITING) {
        "Only waiting publication release readiness can confirm manual release."
    }

    manualConfirmationStatus = ManualReleaseConfirmationStatus.CONFIRMED
}

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
