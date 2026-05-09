package com.only4.cap4k.reference.contentstudio.application.subscribers.integration

import com.only4.cap4k.ddd.core.application.event.annotation.IntegrationEvent

/**
 * Local integration-event contract kept under application subscribers until #34 lands.
 * The contract stays versioned inside the reference project, but it lives in the application-side published boundary.
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