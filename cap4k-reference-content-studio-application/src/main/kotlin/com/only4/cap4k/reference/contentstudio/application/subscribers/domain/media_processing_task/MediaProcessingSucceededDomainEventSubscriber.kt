package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.media_processing_task

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.ContinuePublicationAfterMediaSucceededCmd
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.events.MediaProcessingSucceededDomainEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * media processing succeeded
 */
@Service
class MediaProcessingSucceededDomainEventSubscriber {

    @EventListener(MediaProcessingSucceededDomainEvent::class)
    fun onMediaProcessingSucceeded(event: MediaProcessingSucceededDomainEvent) {
        Mediator.cmd.send(
            ContinuePublicationAfterMediaSucceededCmd.Request(
                contentId = event.contentId,
                mediaProcessingTaskId = event.taskId,
            )
        )
    }
}
