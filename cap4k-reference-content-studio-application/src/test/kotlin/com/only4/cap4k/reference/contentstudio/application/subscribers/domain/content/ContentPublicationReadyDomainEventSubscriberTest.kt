package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.events.ContentPublicationReadyDomainEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.context.event.EventListener

class ContentPublicationReadyDomainEventSubscriberTest {

    @Test
    fun `publication ready domain fact intentionally wakes immediate and paid continuations`() {
        val listenerMethods =
            ContentPublicationReadyDomainEventSubscriber::class.java.declaredMethods
                .filter { method ->
                    method.getAnnotation(EventListener::class.java) != null &&
                        method.parameterTypes.singleOrNull() == ContentPublicationReadyDomainEvent::class.java
                }
                .map { method -> method.name }
                .sorted()

        assertEquals(
            listOf("continueImmediatePublication", "continuePaidPublication"),
            listenerMethods,
        )
    }
}
