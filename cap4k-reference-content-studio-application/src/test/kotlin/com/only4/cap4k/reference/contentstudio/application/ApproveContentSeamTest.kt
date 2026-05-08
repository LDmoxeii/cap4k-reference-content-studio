package com.only4.cap4k.reference.contentstudio.application

import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.ApproveContentReviewCmd
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.StartMediaProcessingCmd
import com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content.ContentReviewApprovedDomainEventSubscriber
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentReviewApprovedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.reviewStatusValue
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.processingStatusValue
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ApproveContentSeamTest {
    @Test
    fun `approval writes only content and starts media processing through the domain-event seam`() {
        val attachedEvents = installTestDomainEventSupervisor()
        val content = contentFixture()
        val contentRepository = InMemoryContentRepository(listOf(content))
        val mediaProcessingTaskRepository = InMemoryMediaProcessingTaskRepository()
        val mediaProcessingCli = FakeMediaProcessingCli(externalTaskId = "ext-approval-1")
        val startHandler =
            StartMediaProcessingCmd.Handler(
                contentRepository = contentRepository,
                mediaProcessingTaskRepository = mediaProcessingTaskRepository,
                mediaProcessingCli = mediaProcessingCli,
            )
        val requestSupervisor =
            RecordingRequestSupervisor().apply {
                register(StartMediaProcessingCmd.Request::class.java, startHandler::exec)
            }
        val subscriber = ContentReviewApprovedDomainEventSubscriber(requestSupervisor)
        val approveHandler = ApproveContentReviewCmd.Handler(contentRepository)
        val reviewerId = UUID.randomUUID()
        val reviewedAt = LocalDateTime.of(2026, 5, 9, 10, 30)

        approveHandler.exec(
            ApproveContentReviewCmd.Request(
                contentId = content.id,
                reviewerId = reviewerId,
                reviewedAt = reviewedAt,
            )
        )

        assertEquals(listOf(content.id), contentRepository.saveCalls)
        assertTrue(mediaProcessingTaskRepository.saveCalls.isEmpty())
        assertTrue(mediaProcessingCli.calls.isEmpty())
        assertEquals(ReviewStatus.APPROVED, contentRepository.require(content.id).reviewStatusValue)

        val approvedEvent = assertSingleContentApprovedEvent(attachedEvents, content.id)
        subscriber.on(approvedEvent)

        assertEquals(
            listOf(StartMediaProcessingCmd.Request(contentId = content.id)),
            requestSupervisor.sentRequests.filterIsInstance<StartMediaProcessingCmd.Request>(),
        )
        val savedTask = mediaProcessingTaskRepository.require(content.id)
        assertEquals(MediaProcessingStatus.SUBMITTED, savedTask.processingStatusValue)
        assertEquals("ext-approval-1", savedTask.externalTaskId)
        assertEquals(
            listOf(FakeMediaProcessingCli.Call(contentId = content.id, mediaSourceKey = content.mediaSourceKey)),
            mediaProcessingCli.calls,
        )
    }

    private fun assertSingleContentApprovedEvent(
        attachedEvents: TestDomainEventSupervisor,
        contentId: UUID,
    ): ContentReviewApprovedDomainEvent {
        assertEquals(1, attachedEvents.attachedEvents.size)
        val approvedEvent = attachedEvents.attachedEvents.single()
        assertTrue(approvedEvent is ContentReviewApprovedDomainEvent)
        approvedEvent as ContentReviewApprovedDomainEvent
        assertEquals(contentId, approvedEvent.contentId)
        return approvedEvent
    }
}
