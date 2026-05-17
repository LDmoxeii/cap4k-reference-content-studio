package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.media_processing_task

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.PublishContentCmd
import com.only4.cap4k.reference.contentstudio.application.commands.release.readiness.OpenPublicationReleaseReadinessCmd
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.events.MediaProcessingSucceededDomainEvent
import java.time.LocalDateTime
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

/**
 * media processing succeeded
 */
@Service
class MediaProcessingSucceededDomainEventSubscriber {

    @EventListener(MediaProcessingSucceededDomainEvent::class)
    fun publishImmediateContent(event: MediaProcessingSucceededDomainEvent) {
        Mediator.cmd.send(
            PublishContentCmd.Request(
                contentId = event.contentId,
                publishedAt = LocalDateTime.now(),
            )
        )
    }

    @EventListener(MediaProcessingSucceededDomainEvent::class)
    fun openGatedPublicationReleaseReadiness(event: MediaProcessingSucceededDomainEvent) {
        Mediator.cmd.send(
            OpenPublicationReleaseReadinessCmd.Request(
                contentId = event.contentId,
                mediaProcessingTaskId = event.taskId,
            )
        )
    }
}
