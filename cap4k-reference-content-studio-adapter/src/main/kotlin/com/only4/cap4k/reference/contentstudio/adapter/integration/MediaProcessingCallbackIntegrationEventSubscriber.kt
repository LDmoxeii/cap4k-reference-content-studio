package com.only4.cap4k.reference.contentstudio.adapter.integration

import com.only4.cap4k.ddd.core.domain.event.EventSubscriber
import com.only4.cap4k.reference.contentstudio.application.transition.MediaProcessingSucceededTransitionSurface
import org.springframework.stereotype.Service

/**
 * Handwritten callback bridge for #34. The real HTTP consume path terminates here and then
 * crosses the explicit application transition surface.
 */
@Service
class MediaProcessingCallbackIntegrationEventSubscriber(
    private val transitionSurface: MediaProcessingSucceededTransitionSurface,
) : EventSubscriber<MediaProcessingCallbackIntegrationEvent> {

    override fun onEvent(event: MediaProcessingCallbackIntegrationEvent) {
        if (event.status.uppercase() != MediaProcessingCallbackIntegrationEvent.SUCCEEDED_STATUS) {
            return
        }

        transitionSurface.on(
            MediaProcessingSucceededTransitionSurface.Event(
                contentId = event.contentId,
                externalTaskId = event.externalTaskId,
            )
        )
    }
}
