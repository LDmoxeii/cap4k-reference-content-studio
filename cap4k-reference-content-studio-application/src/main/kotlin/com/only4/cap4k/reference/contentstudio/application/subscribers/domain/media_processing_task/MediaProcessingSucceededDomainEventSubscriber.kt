package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.media_processing_task

import com.only4.cap4k.ddd.core.application.RequestSupervisor
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.PublishContentCmd
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.events.MediaProcessingSucceededDomainEvent
import java.time.Clock
import java.time.LocalDateTime
import org.springframework.context.event.EventListener
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

/**
 * media processing succeeded
 */
@Service
class MediaProcessingSucceededDomainEventSubscriber(
    @Qualifier("defaultRequestSupervisor")
    private val requestSupervisor: RequestSupervisor,
    private val clock: Clock = Clock.systemUTC(),
) {

    @EventListener(MediaProcessingSucceededDomainEvent::class)
    fun on(event: MediaProcessingSucceededDomainEvent) {
        requestSupervisor.send(
            PublishContentCmd.Request(
                contentId = event.contentId,
                publishedAt = LocalDateTime.ofInstant(clock.instant(), clock.zone),
            )
        )
    }
}
