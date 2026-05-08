package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task

import com.only4.cap4k.reference.contentstudio.domain.TestDomainEventSupervisor
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.events.MediaProcessingSucceededDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.installTestDomainEventSupervisor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class MediaProcessingTaskBehaviorTest {
    private lateinit var domainEvents: TestDomainEventSupervisor

    @BeforeEach
    fun setUp() {
        domainEvents = installTestDomainEventSupervisor()
    }

    @Test
    fun `mark submitted stores external task id and submitted status`() {
        val task = newTask()

        task.markSubmitted(externalTaskId = "external-123")

        assertEquals("external-123", task.externalTaskId)
        assertEquals(MediaProcessingStatus.SUBMITTED.name, task.processingStatus)
        assertEquals(emptyList<Any>(), domainEvents.attachedEvents)
    }

    @Test
    fun `mark processing succeeded emits media processing succeeded event`() {
        val task = newTask(
            externalTaskId = "external-123",
            processingStatus = MediaProcessingStatus.SUBMITTED.name,
        )

        task.markSucceeded()

        assertEquals(MediaProcessingStatus.SUCCEEDED.name, task.processingStatus)

        val event = assertInstanceOf(
            MediaProcessingSucceededDomainEvent::class.java,
            domainEvents.attachedEvents.single(),
        )
        assertEquals(task.id, event.taskId)
        assertEquals(task.contentId, event.contentId)
        assertEquals("external-123", event.externalTaskId)
    }

    private fun newTask(
        externalTaskId: String? = null,
        processingStatus: String = MediaProcessingStatus.PENDING.name,
    ): MediaProcessingTask {
        val now = LocalDateTime.of(2026, 5, 9, 9, 0)
        return MediaProcessingTask(
            id = UUID.randomUUID(),
            contentId = UUID.randomUUID(),
            externalTaskId = externalTaskId,
            processingStatus = processingStatus,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }
}
