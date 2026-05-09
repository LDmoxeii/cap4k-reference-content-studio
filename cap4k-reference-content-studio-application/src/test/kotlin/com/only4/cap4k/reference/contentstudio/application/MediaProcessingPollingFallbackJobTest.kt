package com.only4.cap4k.reference.contentstudio.application

import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.MarkMediaProcessingSucceededCmd
import com.only4.cap4k.reference.contentstudio.application.jobs.MediaProcessingPollingFallbackJob
import com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingCli
import com.only4.cap4k.reference.contentstudio.application.transition.MediaProcessingSucceededTransitionSurface
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.processingStatusValue
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MediaProcessingPollingFallbackJobTest {

    @Test
    fun `polling fallback marks submitted task succeeded when external status completes`() {
        installTestDomainEventSupervisor()
        val contentId = UUID.randomUUID()
        val mediaProcessingTaskRepository =
            InMemoryMediaProcessingTaskRepository(
                initial =
                    listOf(
                        mediaProcessingTaskFixture(
                            contentId = contentId,
                            status = MediaProcessingStatus.SUBMITTED,
                            externalTaskId = "ext-poll-1",
                        )
                    )
            )
        val mediaProcessingCli =
            FakeMediaProcessingCli(
                externalTaskId = "ext-poll-1",
                polledStatuses =
                    mapOf(
                        "ext-poll-1" to MediaProcessingCli.StatusResponse(MediaProcessingCli.ExternalTaskStatus.SUCCEEDED)
                    ),
            )
        val markHandler = MarkMediaProcessingSucceededCmd.Handler(mediaProcessingTaskRepository)
        val requestSupervisor =
            RecordingRequestSupervisor().apply {
                register(MarkMediaProcessingSucceededCmd.Request::class.java, markHandler::exec)
            }
        val transitionSurface = MediaProcessingSucceededTransitionSurface(requestSupervisor)
        val job =
            MediaProcessingPollingFallbackJob(
                mediaProcessingTaskRepository = mediaProcessingTaskRepository,
                mediaProcessingCli = mediaProcessingCli,
                transitionSurface = transitionSurface,
            )

        job.pollSubmittedTasks()

        assertEquals(listOf("ext-poll-1"), mediaProcessingCli.statusCalls)
        assertEquals(
            listOf(MarkMediaProcessingSucceededCmd.Request(externalTaskId = "ext-poll-1")),
            requestSupervisor.sentRequests.filterIsInstance<MarkMediaProcessingSucceededCmd.Request>(),
        )
        assertEquals(MediaProcessingStatus.SUCCEEDED, mediaProcessingTaskRepository.require(contentId).processingStatusValue)
    }
}
