package com.only4.cap4k.reference.contentstudio.application.transition

import com.only4.cap4k.ddd.core.application.RequestSupervisor
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.MarkMediaProcessingSucceededCmd
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

/**
 * Application-local handwritten transition surface until design-driven integration events land upstream.
 */
@Service
class MediaProcessingSucceededTransitionSurface(
    @Qualifier("defaultRequestSupervisor")
    private val requestSupervisor: RequestSupervisor,
) {
    fun on(event: Event) {
        requestSupervisor.send(
            MarkMediaProcessingSucceededCmd.Request(
                externalTaskId = event.externalTaskId,
            )
        )
    }

    data class Event(
        val externalTaskId: String,
    )
}
