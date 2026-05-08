package com.only4.cap4k.reference.contentstudio.application

import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.PublishContentCmd
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.MarkMediaProcessingSucceededCmd
import com.only4.cap4k.reference.contentstudio.application.subscribers.domain.media_processing_task.MediaProcessingSucceededDomainEventSubscriber
import com.only4.cap4k.reference.contentstudio.application.transition.MediaProcessingSucceededTransitionSurface
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.contentStatusValue
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentPublishedDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.events.MediaProcessingSucceededDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.processingStatusValue
import com.only4.cap4k.reference.contentstudio.domain.services.PublicationEligibilityDomainService
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PublishContentFlowTest {
    @Test
    fun `publish happens only after the explicit callback transition and media-processing domain-event seam`() {
        val attachedEvents = installTestDomainEventSupervisor()
        val reviewedAt = LocalDateTime.of(2026, 5, 9, 11, 0)
        val content =
            contentFixture(
                reviewStatus = ReviewStatus.APPROVED,
                contentStatus = ContentStatus.DRAFT,
                reviewerId = UUID.randomUUID(),
                reviewedAt = reviewedAt,
            )
        val task =
            mediaProcessingTaskFixture(
                contentId = content.id,
                status = MediaProcessingStatus.SUBMITTED,
                externalTaskId = "ext-publish-1",
            )
        val contentRepository = InMemoryContentRepository(listOf(content))
        val mediaProcessingTaskRepository = InMemoryMediaProcessingTaskRepository(listOf(task))
        val publishHandler =
            PublishContentCmd.Handler(
                contentRepository = contentRepository,
                mediaProcessingTaskRepository = mediaProcessingTaskRepository,
                publicationEligibilityDomainService = PublicationEligibilityDomainService(),
            )
        val markHandler = MarkMediaProcessingSucceededCmd.Handler(mediaProcessingTaskRepository)
        val requestSupervisor =
            RecordingRequestSupervisor().apply {
                register(MarkMediaProcessingSucceededCmd.Request::class.java, markHandler::exec)
                register(PublishContentCmd.Request::class.java, publishHandler::exec)
            }
        val clock = Clock.fixed(Instant.parse("2026-05-09T04:05:06Z"), ZoneOffset.UTC)
        val transitionSurface = MediaProcessingSucceededTransitionSurface(requestSupervisor)
        val subscriber = MediaProcessingSucceededDomainEventSubscriber(requestSupervisor, clock)

        transitionSurface.on(
            MediaProcessingSucceededTransitionSurface.Event(
                contentId = content.id,
                externalTaskId = "ext-publish-1",
            )
        )

        assertEquals(
            listOf(
                MarkMediaProcessingSucceededCmd.Request(
                    contentId = content.id,
                    externalTaskId = "ext-publish-1",
                )
            ),
            requestSupervisor.sentRequests.filterIsInstance<MarkMediaProcessingSucceededCmd.Request>(),
        )
        assertEquals(listOf(content.id), mediaProcessingTaskRepository.saveCalls)
        assertTrue(contentRepository.saveCalls.isEmpty())
        assertEquals(MediaProcessingStatus.SUCCEEDED, mediaProcessingTaskRepository.require(content.id).processingStatusValue)

        val succeededEvent = assertSingleMediaProcessingSucceededEvent(attachedEvents, content.id)
        subscriber.on(succeededEvent)

        val publishedAt = LocalDateTime.ofInstant(clock.instant(), clock.zone)
        assertEquals(
            listOf(
                PublishContentCmd.Request(
                    contentId = content.id,
                    publishedAt = publishedAt,
                )
            ),
            requestSupervisor.sentRequests.filterIsInstance<PublishContentCmd.Request>(),
        )
        assertEquals(listOf(content.id), contentRepository.saveCalls)
        assertEquals(listOf(content.id), mediaProcessingTaskRepository.saveCalls)
        assertEquals(ContentStatus.PUBLISHED, contentRepository.require(content.id).contentStatusValue)
        assertTrue(attachedEvents.attachedEvents.any { it is ContentPublishedDomainEvent })
    }

    @Test
    fun `temporary callback seam rejects mismatched external task id and does not publish`() {
        val attachedEvents = installTestDomainEventSupervisor()
        val content =
            contentFixture(
                reviewStatus = ReviewStatus.APPROVED,
                contentStatus = ContentStatus.DRAFT,
                reviewerId = UUID.randomUUID(),
                reviewedAt = LocalDateTime.of(2026, 5, 9, 11, 0),
            )
        val task =
            mediaProcessingTaskFixture(
                contentId = content.id,
                status = MediaProcessingStatus.SUBMITTED,
                externalTaskId = "ext-expected",
            )
        val contentRepository = InMemoryContentRepository(listOf(content))
        val mediaProcessingTaskRepository = InMemoryMediaProcessingTaskRepository(listOf(task))
        val publishHandler =
            PublishContentCmd.Handler(
                contentRepository = contentRepository,
                mediaProcessingTaskRepository = mediaProcessingTaskRepository,
                publicationEligibilityDomainService = PublicationEligibilityDomainService(),
            )
        val markHandler = MarkMediaProcessingSucceededCmd.Handler(mediaProcessingTaskRepository)
        val requestSupervisor =
            RecordingRequestSupervisor().apply {
                register(MarkMediaProcessingSucceededCmd.Request::class.java, markHandler::exec)
                register(PublishContentCmd.Request::class.java, publishHandler::exec)
            }
        val transitionSurface = MediaProcessingSucceededTransitionSurface(requestSupervisor)

        val error =
            assertThrows(IllegalStateException::class.java) {
                transitionSurface.on(
                    MediaProcessingSucceededTransitionSurface.Event(
                        contentId = content.id,
                        externalTaskId = "ext-wrong",
                    )
                )
            }

        assertTrue(error.message!!.contains("External task id mismatch"))
        assertEquals(
            listOf(
                MarkMediaProcessingSucceededCmd.Request(
                    contentId = content.id,
                    externalTaskId = "ext-wrong",
                )
            ),
            requestSupervisor.sentRequests,
        )
        assertTrue(attachedEvents.attachedEvents.isEmpty())
        assertTrue(contentRepository.saveCalls.isEmpty())
        assertTrue(mediaProcessingTaskRepository.saveCalls.isEmpty())
        assertEquals(ContentStatus.DRAFT, contentRepository.require(content.id).contentStatusValue)
        assertEquals(MediaProcessingStatus.SUBMITTED, mediaProcessingTaskRepository.require(content.id).processingStatusValue)
    }

    private fun assertSingleMediaProcessingSucceededEvent(
        attachedEvents: TestDomainEventSupervisor,
        contentId: UUID,
    ): MediaProcessingSucceededDomainEvent {
        assertEquals(1, attachedEvents.attachedEvents.size)
        val succeededEvent = attachedEvents.attachedEvents.single()
        assertTrue(succeededEvent is MediaProcessingSucceededDomainEvent)
        succeededEvent as MediaProcessingSucceededDomainEvent
        assertEquals(contentId, succeededEvent.contentId)
        return succeededEvent
    }
}
