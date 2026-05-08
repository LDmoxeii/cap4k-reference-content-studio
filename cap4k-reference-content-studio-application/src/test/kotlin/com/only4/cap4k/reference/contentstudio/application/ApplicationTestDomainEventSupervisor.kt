package com.only4.cap4k.reference.contentstudio.application

import com.only4.cap4k.ddd.core.domain.event.DomainEventSupervisor
import com.only4.cap4k.ddd.core.domain.event.DomainEventSupervisorSupport
import java.time.LocalDateTime

internal class TestDomainEventSupervisor : DomainEventSupervisor {
    private val events = mutableListOf<Any>()

    val attachedEvents: List<Any>
        get() = events.toList()

    override fun <DOMAIN_EVENT : Any, ENTITY : Any> attach(
        domainEventPayload: DOMAIN_EVENT,
        entity: ENTITY,
        schedule: LocalDateTime,
    ) {
        events += domainEventPayload
    }

    override fun <DOMAIN_EVENT : Any, ENTITY : Any> attach(
        entity: ENTITY,
        schedule: LocalDateTime,
        domainEventPayloadSupplier: () -> DOMAIN_EVENT,
    ) {
        events += domainEventPayloadSupplier()
    }

    override fun <DOMAIN_EVENT : Any, ENTITY : Any> detach(
        domainEventPayload: DOMAIN_EVENT,
        entity: ENTITY,
    ) = Unit
}

internal fun installTestDomainEventSupervisor(): TestDomainEventSupervisor =
    TestDomainEventSupervisor().also(DomainEventSupervisorSupport::configure)
