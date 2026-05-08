package com.only4.cap4k.reference.contentstudio.application

import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.ApproveContentReviewCmd
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.CreateContentDraftCmd
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.PublishContentCmd
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.SubmitContentForReviewCmd
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.MarkMediaProcessingSucceededCmd
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.StartMediaProcessingCmd
import com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content.ContentReviewApprovedDomainEventSubscriber
import com.only4.cap4k.reference.contentstudio.application.subscribers.domain.media_processing_task.MediaProcessingSucceededDomainEventSubscriber
import com.only4.cap4k.reference.contentstudio.application.transition.MediaProcessingSucceededTransitionSurface
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentDraftCreatedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentReviewApprovedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentSubmittedForReviewDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.contentStatusValue
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.reviewStatusValue
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.events.MediaProcessingSucceededDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.processingStatusValue
import com.only4.cap4k.reference.contentstudio.domain.services.PublicationEligibilityDomainService
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DefaultPathApplicationFlowTest {
    @Test
    fun `application default path runs from create to publish`() {
        val domainEvents = installTestDomainEventSupervisor()
        val contentRepository = InMemoryContentRepository()
        val mediaProcessingTaskRepository = InMemoryMediaProcessingTaskRepository()
        val mediaProcessingCli = FakeMediaProcessingCli(externalTaskId = "ext-full-1")
        val createHandler = CreateContentDraftCmd.Handler(contentRepository)
        val submitHandler = SubmitContentForReviewCmd.Handler(contentRepository)
        val approveHandler = ApproveContentReviewCmd.Handler(contentRepository)
        val startHandler =
            StartMediaProcessingCmd.Handler(
                contentRepository = contentRepository,
                mediaProcessingTaskRepository = mediaProcessingTaskRepository,
                mediaProcessingCli = mediaProcessingCli,
            )
        val markHandler = MarkMediaProcessingSucceededCmd.Handler(mediaProcessingTaskRepository)
        val publishHandler =
            PublishContentCmd.Handler(
                contentRepository = contentRepository,
                mediaProcessingTaskRepository = mediaProcessingTaskRepository,
                publicationEligibilityDomainService = PublicationEligibilityDomainService(),
            )
        val requestSupervisor =
            RecordingRequestSupervisor().apply {
                register(StartMediaProcessingCmd.Request::class.java, startHandler::exec)
                register(MarkMediaProcessingSucceededCmd.Request::class.java, markHandler::exec)
                register(PublishContentCmd.Request::class.java, publishHandler::exec)
            }
        val approvalSubscriber = ContentReviewApprovedDomainEventSubscriber(requestSupervisor)
        val clock = Clock.fixed(Instant.parse("2026-05-09T05:06:07Z"), ZoneOffset.UTC)
        val mediaSucceededSubscriber = MediaProcessingSucceededDomainEventSubscriber(requestSupervisor, clock)
        val transitionSurface = MediaProcessingSucceededTransitionSurface(requestSupervisor)

        val createResponse =
            createHandler.exec(
                CreateContentDraftCmd.Request(
                    title = "The strict path",
                    body = "Reference proof",
                    mediaSourceKey = "media/full-path.mp4",
                )
            )
        val contentId = createResponse.contentId
        val createdContent = contentRepository.require(contentId)
        assertEquals(ReviewStatus.PENDING, createdContent.reviewStatusValue)
        assertEquals(ContentStatus.DRAFT, createdContent.contentStatusValue)
        assertEquals(listOf(contentId), contentRepository.saveCalls)
        assertEquals(contentId, domainEvents.attachedEvents.single(ContentDraftCreatedDomainEvent::class.java).contentId)
        domainEvents.clear()

        submitHandler.exec(SubmitContentForReviewCmd.Request(contentId = contentId))

        assertEquals(listOf(contentId, contentId), contentRepository.saveCalls)
        assertEquals(contentId, domainEvents.attachedEvents.single(ContentSubmittedForReviewDomainEvent::class.java).contentId)
        domainEvents.clear()

        val reviewedAt = LocalDateTime.of(2026, 5, 9, 12, 0)
        approveHandler.exec(
            ApproveContentReviewCmd.Request(
                contentId = contentId,
                reviewerId = UUID.randomUUID(),
                reviewedAt = reviewedAt,
            )
        )

        val approvedEvent = domainEvents.attachedEvents.single(ContentReviewApprovedDomainEvent::class.java)
        approvalSubscriber.on(approvedEvent)
        assertEquals(listOf(contentId, contentId, contentId), contentRepository.saveCalls)
        assertEquals(
            listOf(StartMediaProcessingCmd.Request(contentId = contentId)),
            requestSupervisor.sentRequests.filterIsInstance<StartMediaProcessingCmd.Request>(),
        )
        val taskAfterStart = mediaProcessingTaskRepository.require(contentId)
        assertEquals(MediaProcessingStatus.SUBMITTED, taskAfterStart.processingStatusValue)
        assertEquals("ext-full-1", taskAfterStart.externalTaskId)
        domainEvents.clear()

        transitionSurface.on(
            MediaProcessingSucceededTransitionSurface.Event(
                externalTaskId = "ext-full-1",
            )
        )

        assertEquals(
            listOf(
                StartMediaProcessingCmd.Request(contentId = contentId),
                MarkMediaProcessingSucceededCmd.Request(
                    externalTaskId = "ext-full-1",
                ),
            ),
            requestSupervisor.sentRequests.filter {
                it is StartMediaProcessingCmd.Request || it is MarkMediaProcessingSucceededCmd.Request
            },
        )
        val mediaSucceededEvent = domainEvents.attachedEvents.single(MediaProcessingSucceededDomainEvent::class.java)
        mediaSucceededSubscriber.on(mediaSucceededEvent)

        val publishedContent = contentRepository.require(contentId)
        assertEquals(ContentStatus.PUBLISHED, publishedContent.contentStatusValue)
        assertEquals(ReviewStatus.APPROVED, publishedContent.reviewStatusValue)
        assertEquals(listOf(contentId, contentId, contentId, contentId), contentRepository.saveCalls)
        assertEquals(listOf(contentId, contentId), mediaProcessingTaskRepository.saveCalls)
        assertEquals(
            listOf(
                StartMediaProcessingCmd.Request(contentId = contentId),
                MarkMediaProcessingSucceededCmd.Request(
                    externalTaskId = "ext-full-1",
                ),
                PublishContentCmd.Request(
                    contentId = contentId,
                    publishedAt = LocalDateTime.ofInstant(clock.instant(), clock.zone),
                ),
            ),
            requestSupervisor.sentRequests,
        )
        assertEquals(
            listOf(FakeMediaProcessingCli.Call(contentId = contentId, mediaSourceKey = "media/full-path.mp4")),
            mediaProcessingCli.calls,
        )
    }

    private fun <T : Any> List<Any>.single(type: Class<T>): T {
        val matching = filterIsInstance(type)
        assertEquals(1, matching.size)
        return matching.single()
    }

    private fun <T : Any> List<Any>.filterIsInstance(type: Class<T>): List<T> =
        mapNotNull { candidate -> type.takeIf { it.isInstance(candidate) }?.cast(candidate) }
}
