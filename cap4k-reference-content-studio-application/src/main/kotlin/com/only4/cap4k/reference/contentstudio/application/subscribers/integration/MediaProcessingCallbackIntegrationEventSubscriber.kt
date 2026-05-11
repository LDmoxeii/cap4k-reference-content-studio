package com.only4.cap4k.reference.contentstudio.application.subscribers.integration

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.domain.event.EventSubscriber
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.MarkMediaProcessingSucceededCmd
import org.springframework.stereotype.Service

/**
 * Handwritten callback bridge for #34. The real HTTP consume path terminates here and
 * forwards into the internal command seam.
 */
@Service
class MediaProcessingCallbackIntegrationEventSubscriber : EventSubscriber<MediaProcessingCallbackIntegrationEvent> {

    override fun onEvent(event: MediaProcessingCallbackIntegrationEvent) {
        if (event.status.uppercase() != MediaProcessingCallbackIntegrationEvent.SUCCEEDED_STATUS) {
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
}
