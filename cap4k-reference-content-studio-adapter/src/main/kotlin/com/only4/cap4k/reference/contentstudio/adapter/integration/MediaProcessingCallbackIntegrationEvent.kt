package com.only4.cap4k.reference.contentstudio.adapter.integration

import com.only4.cap4k.ddd.core.application.event.annotation.IntegrationEvent

/**
 * Handwritten integration-event contract kept local until design-driven event families land for #34.
 */
@IntegrationEvent(
    value = MediaProcessingCallbackIntegrationEvent.EVENT_NAME,
    subscriber = MediaProcessingCallbackIntegrationEvent.SUBSCRIBER_NAME,
)
data class MediaProcessingCallbackIntegrationEvent(
    val externalTaskId: String,
    val status: String,
) {
    companion object {
        const val EVENT_NAME = "cap4k.reference.contentstudio.media-processing.succeeded"
        const val SUBSCRIBER_NAME = "cap4k-reference-content-studio"
        const val SUCCEEDED_STATUS = "SUCCEEDED"
    }
}
