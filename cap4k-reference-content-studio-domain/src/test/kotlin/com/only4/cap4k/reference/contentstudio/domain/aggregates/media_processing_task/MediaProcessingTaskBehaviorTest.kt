package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task

import com.only4.cap4k.ddd.core.domain.event.annotation.DomainEvent
import com.only4.cap4k.reference.contentstudio.domain.TestDomainEventSupervisor
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.events.MediaProcessingSucceededDomainEvent
import com.only4.cap4k.reference.contentstudio.domain.installTestDomainEventSupervisor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
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
        assertEquals(MediaProcessingStatus.SUBMITTED, task.processingStatus)
        assertEquals(emptyList<Any>(), domainEvents.attachedEvents)
    }

    @Test
    fun `mark submitted rejects blank external task id`() {
        val task = newTask()

        assertThrows(IllegalStateException::class.java) {
            task.markSubmitted(externalTaskId = "   ")
        }

        assertEquals(null, task.externalTaskId)
        assertEquals(MediaProcessingStatus.PENDING, task.processingStatus)
        assertEquals(emptyList<Any>(), domainEvents.attachedEvents)
    }

    @Test
    fun `mark submitted is a no-op for duplicate submission with same external task id`() {
        val task = newTask(
            externalTaskId = "external-123",
            processingStatus = MediaProcessingStatus.SUBMITTED,
        )

        task.markSubmitted(externalTaskId = "external-123")

        assertEquals("external-123", task.externalTaskId)
        assertEquals(MediaProcessingStatus.SUBMITTED, task.processingStatus)
        assertEquals(emptyList<Any>(), domainEvents.attachedEvents)
    }

    @Test
    fun `mark submitted rejects duplicate submission with different external task id`() {
        val task = newTask(
            externalTaskId = "external-123",
            processingStatus = MediaProcessingStatus.SUBMITTED,
        )

        assertThrows(IllegalStateException::class.java) {
            task.markSubmitted(externalTaskId = "external-456")
        }

        assertEquals("external-123", task.externalTaskId)
        assertEquals(MediaProcessingStatus.SUBMITTED, task.processingStatus)
        assertEquals(emptyList<Any>(), domainEvents.attachedEvents)
    }

    @Test
    fun `mark processing succeeded emits media processing succeeded event`() {
        val task = newTask(
            externalTaskId = "external-123",
            processingStatus = MediaProcessingStatus.SUBMITTED,
        )

        task.markSucceeded()

        assertEquals(MediaProcessingStatus.SUCCEEDED, task.processingStatus)

        val event = assertInstanceOf(
            MediaProcessingSucceededDomainEvent::class.java,
            domainEvents.attachedEvents.single(),
        )
        assertEquals(task.id, event.taskId)
        assertEquals(task.contentId, event.contentId)
        assertEquals("external-123", event.externalTaskId)
    }

    @Test
    fun `mark processing succeeded is a no-op when task is already succeeded`() {
        val task = newTask(
            externalTaskId = "external-123",
            processingStatus = MediaProcessingStatus.SUCCEEDED,
        )

        task.markSucceeded()

        assertEquals(MediaProcessingStatus.SUCCEEDED, task.processingStatus)
        assertEquals(emptyList<Any>(), domainEvents.attachedEvents)
    }

    @Test
    fun `mark submitted rejects regressing a succeeded task`() {
        val task = newTask(
            externalTaskId = "external-123",
            processingStatus = MediaProcessingStatus.SUCCEEDED,
        )

        assertThrows(IllegalStateException::class.java) {
            task.markSubmitted(externalTaskId = "external-456")
        }

        assertEquals("external-123", task.externalTaskId)
        assertEquals(MediaProcessingStatus.SUCCEEDED, task.processingStatus)
        assertEquals(emptyList<Any>(), domainEvents.attachedEvents)
    }

    @Test
    fun `mark processing succeeded rejects pending task`() {
        val task = newTask()

        assertThrows(IllegalStateException::class.java) {
            task.markSucceeded()
        }

        assertEquals(MediaProcessingStatus.PENDING, task.processingStatus)
        assertEquals(emptyList<Any>(), domainEvents.attachedEvents)
    }

    @Test
    fun `mark processing succeeded rejects blank external task id`() {
        val task = newTask(
            externalTaskId = "   ",
            processingStatus = MediaProcessingStatus.SUBMITTED,
        )

        assertThrows(IllegalStateException::class.java) {
            task.markSucceeded()
        }

        assertEquals("   ", task.externalTaskId)
        assertEquals(MediaProcessingStatus.SUBMITTED, task.processingStatus)
        assertEquals(emptyList<Any>(), domainEvents.attachedEvents)
    }

    @Test
    fun `media processing succeeded event is durable`() {
        val annotation = MediaProcessingSucceededDomainEvent::class.java.getAnnotation(DomainEvent::class.java)

        assertTrue(annotation.persist)
    }

    private fun newTask(
        externalTaskId: String? = null,
        processingStatus: MediaProcessingStatus = MediaProcessingStatus.PENDING,
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
