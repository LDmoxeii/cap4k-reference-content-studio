package com.only4.cap4k.reference.contentstudio.adapter.application.distributed.clients.paid.publication

import java.util.concurrent.atomic.AtomicBoolean
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class FakePaidPublicationCliState(
    @Value("\${contentStudio.fakeEntitlement.failActivation:false}") initialFailActivation: Boolean
) {
    private val failActivation = AtomicBoolean(initialFailActivation)

    fun shouldFailActivation(): Boolean = failActivation.get()

    fun setFailActivation(value: Boolean) {
        failActivation.set(value)
    }
}
