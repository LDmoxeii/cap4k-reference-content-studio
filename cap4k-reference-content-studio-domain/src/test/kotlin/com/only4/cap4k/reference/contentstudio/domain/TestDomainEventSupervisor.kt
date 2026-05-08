package com.only4.cap4k.reference.contentstudio.domain

import com.only4.cap4k.ddd.core.domain.event.DomainEventSupervisor
import com.only4.cap4k.ddd.core.domain.event.DomainEventSupervisorSupport
import java.time.Duration
import java.time.LocalDateTime

class TestDomainEventSupervisor : DomainEventSupervisor {
    private val _attachedEvents = mutableListOf<Any>()

    val attachedEvents: List<Any>
        get() = _attachedEvents.toList()

    override fun <DOMAIN_EVENT : Any, ENTITY : Any> attach(
        domainEventPayload: DOMAIN_EVENT,
        entity: ENTITY,
        schedule: LocalDateTime,
    ) {
        _attachedEvents += domainEventPayload
    }

    override fun <DOMAIN_EVENT : Any, ENTITY : Any> attach(
        entity: ENTITY,
        schedule: LocalDateTime,
        domainEventPayloadSupplier: () -> DOMAIN_EVENT,
    ) {
        _attachedEvents += domainEventPayloadSupplier()
    }

    override fun <DOMAIN_EVENT : Any, ENTITY : Any> detach(domainEventPayload: DOMAIN_EVENT, entity: ENTITY) = Unit

    fun clear() {
        _attachedEvents.clear()
    }
}

fun installTestDomainEventSupervisor(): TestDomainEventSupervisor {
    val supervisor = TestDomainEventSupervisor()
    DomainEventSupervisorSupport.configure(supervisor)
    return supervisor
}
