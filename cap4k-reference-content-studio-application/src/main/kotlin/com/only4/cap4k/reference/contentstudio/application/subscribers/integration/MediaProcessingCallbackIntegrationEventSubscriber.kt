package com.only4.cap4k.reference.contentstudio.application.subscribers.integration

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.MarkMediaProcessingSucceededCmd
import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.inbound.media.processing.MediaProcessingCallbackIntegrationEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * media processing callback from external media service
 */
@Service
class MediaProcessingCallbackIntegrationEventSubscriber {

    @EventListener(MediaProcessingCallbackIntegrationEvent::class)
    fun markMediaProcessingSucceeded(event: MediaProcessingCallbackIntegrationEvent) {
        if (event.status.uppercase() !in SUCCESS_STATUSES) {
            return
        }

        Mediator.cmd.send(
            MarkMediaProcessingSucceededCmd.Request(
                externalTaskId = event.externalTaskId,
                assetSha256 = requireNotNull(event.assetSha256) {
                    "Successful media processing callback must include assetSha256."
                },
                assetLocation = requireNotNull(event.assetLocation) {
                    "Successful media processing callback must include assetLocation."
                },
                completedAt = event.completedAt,
            )
        )
    }

    companion object {
        private val SUCCESS_STATUSES = setOf("SUCCEEDED", "COMPLETED")
    }
}
