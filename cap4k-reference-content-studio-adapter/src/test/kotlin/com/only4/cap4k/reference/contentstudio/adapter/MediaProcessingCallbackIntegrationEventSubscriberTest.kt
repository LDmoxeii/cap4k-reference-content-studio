package com.only4.cap4k.reference.contentstudio.adapter

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.RequestSupervisor
import com.only4.cap4k.reference.contentstudio.adapter.integration.MediaProcessingCallbackIntegrationEvent
import com.only4.cap4k.reference.contentstudio.adapter.integration.MediaProcessingCallbackIntegrationEventSubscriber
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.MarkMediaProcessingSucceededCmd
import com.only4.cap4k.reference.contentstudio.application.transition.MediaProcessingSucceededTransitionSurface
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MediaProcessingCallbackIntegrationEventSubscriberTest {
    @Test
    fun `subscriber forwards succeeded callback into transition surface`() {
        val requestSupervisor = RecordingRequestSupervisor()
        val subscriber =
            MediaProcessingCallbackIntegrationEventSubscriber(
                MediaProcessingSucceededTransitionSurface(requestSupervisor)
            )

        subscriber.onEvent(
            MediaProcessingCallbackIntegrationEvent(
                externalTaskId = "ext-forward-1",
                status = "SUCCEEDED",
            )
        )

        assertEquals(
            listOf(
                MarkMediaProcessingSucceededCmd.Request(
                    externalTaskId = "ext-forward-1",
                )
            ),
            requestSupervisor.sentRequests,
        )
    }

    @Test
    fun `subscriber ignores non succeeded callback`() {
        val requestSupervisor = RecordingRequestSupervisor()
        val subscriber =
            MediaProcessingCallbackIntegrationEventSubscriber(
                MediaProcessingSucceededTransitionSurface(requestSupervisor)
            )

        subscriber.onEvent(
            MediaProcessingCallbackIntegrationEvent(
                externalTaskId = "ext-forward-2",
                status = "FAILED",
            )
        )

        assertEquals(emptyList<RequestParam<*>>(), requestSupervisor.sentRequests)
    }

    private class RecordingRequestSupervisor : RequestSupervisor {
        val sentRequests = mutableListOf<RequestParam<*>>()

        override fun <REQUEST : RequestParam<RESPONSE>, RESPONSE : Any> send(request: REQUEST): RESPONSE {
            sentRequests += request
            @Suppress("UNCHECKED_CAST")
            return when (request) {
                is MarkMediaProcessingSucceededCmd.Request -> MarkMediaProcessingSucceededCmd.Response as RESPONSE
                else -> error("No handler registered for ${request::class.java.name}")
            }
        }

        override fun <REQUEST : RequestParam<RESPONSE>, RESPONSE : Any> schedule(
            request: REQUEST,
            schedule: LocalDateTime,
        ): String = error("Scheduling is not used in these adapter tests.")

        override fun <R : Any> result(requestId: String): R? = error("Results are not used in these adapter tests.")
    }
}
