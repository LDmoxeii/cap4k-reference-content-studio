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
        if (event.status.uppercase() != SUCCEEDED_STATUS) {
            return
        }

        Mediator.cmd.send(
            MarkMediaProcessingSucceededCmd.Request(
                externalTaskId = event.externalTaskId,
                assetSha256 = event.assetSha256,
                assetLocation = event.assetLocation,
                completedAt = event.completedAt,
            )
        )
    }

    companion object {
        private const val SUCCEEDED_STATUS = "SUCCEEDED"
    }
}
