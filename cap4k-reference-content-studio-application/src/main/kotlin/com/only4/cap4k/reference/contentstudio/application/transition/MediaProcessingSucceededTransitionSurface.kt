package com.only4.cap4k.reference.contentstudio.application.transition

import com.only4.cap4k.ddd.core.application.RequestSupervisor
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.MarkMediaProcessingSucceededCmd
import java.util.UUID
import org.springframework.stereotype.Service

/**
 * Application-local handwritten transition surface until design-driven integration events land upstream.
 */
@Service
class MediaProcessingSucceededTransitionSurface(
    private val requestSupervisor: RequestSupervisor = RequestSupervisor.instance,
) {
    fun on(event: Event) {
        requestSupervisor.send(
            MarkMediaProcessingSucceededCmd.Request(
                contentId = event.contentId,
                externalTaskId = event.externalTaskId,
            )
        )
    }

    data class Event(
        val contentId: UUID,
        val externalTaskId: String?,
    )
}
