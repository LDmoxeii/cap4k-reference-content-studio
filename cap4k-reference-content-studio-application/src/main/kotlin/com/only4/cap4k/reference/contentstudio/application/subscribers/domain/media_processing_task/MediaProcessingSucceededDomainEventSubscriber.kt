package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.media_processing_task

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.RecordContentMediaReadyCmd
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.events.MediaProcessingSucceededDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * media processing succeeded
 */
@Service
class MediaProcessingSucceededDomainEventSubscriber {

    @EventListener(MediaProcessingSucceededDomainEvent::class)
    fun recordContentMediaReady(event: MediaProcessingSucceededDomainEvent) {
        Mediator.cmd.send(
            RecordContentMediaReadyCmd.Request(
                contentId = event.contentId,
                mediaReadyAt = LocalDateTime.now(),
            ),
        )
    }
}
