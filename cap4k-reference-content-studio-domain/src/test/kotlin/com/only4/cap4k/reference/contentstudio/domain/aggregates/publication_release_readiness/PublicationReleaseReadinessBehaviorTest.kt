package com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness

import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.CopyrightReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.ManualReleaseConfirmationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.PublicationReleaseReadinessState
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PublicationReleaseReadinessBehaviorTest {

    @Test
    fun `complete marks waiting readiness ready when copyright passed manual confirmed and release window is open`() {
        val readiness = waitingReadiness()
        val completedAt = LocalDateTime.of(2026, 5, 10, 10, 30)

        readiness.passCopyrightReview()
        readiness.confirmManualRelease()
        readiness.complete(completedAt)

        assertEquals(PublicationReleaseReadinessState.READY, readiness.readinessState)
        assertEquals(CopyrightReviewStatus.PASSED, readiness.copyrightStatus)
        assertEquals(ManualReleaseConfirmationStatus.CONFIRMED, readiness.manualConfirmationStatus)
        assertEquals(completedAt, readiness.readyAt)
        assertEquals(completedAt, readiness.dbUpdatedAt)
    }

    @Test
    fun `complete rejects readiness before release window opens`() {
        val readiness = waitingReadiness()
        val completedAt = LocalDateTime.of(2026, 5, 10, 8, 59)

        readiness.passCopyrightReview()
        readiness.confirmManualRelease()

        assertThrows(IllegalStateException::class.java) {
            readiness.complete(completedAt)
        }
    }

    private fun waitingReadiness(): PublicationReleaseReadiness =
        PublicationReleaseReadiness(
            id = UUID.randomUUID(),
            contentId = UUID.randomUUID(),
            mediaProcessingTaskId = UUID.randomUUID(),
            readinessState = PublicationReleaseReadinessState.WAITING,
            copyrightStatus = CopyrightReviewStatus.WAITING,
            manualConfirmationStatus = ManualReleaseConfirmationStatus.WAITING,
            releaseWindowOpensAt = LocalDateTime.of(2026, 5, 10, 9, 0),
            releaseWindowClosesAt = LocalDateTime.of(2026, 5, 10, 18, 0),
            readyAt = null,
            cancelReason = null,
            dbCreatedAt = LocalDateTime.of(2026, 5, 9, 12, 0),
            dbUpdatedAt = LocalDateTime.of(2026, 5, 9, 12, 0),
        )
}
