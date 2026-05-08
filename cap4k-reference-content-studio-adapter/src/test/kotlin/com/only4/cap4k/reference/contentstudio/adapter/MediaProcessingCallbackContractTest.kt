package com.only4.cap4k.reference.contentstudio.adapter

import com.only4.cap4k.reference.contentstudio.adapter.integration.MediaProcessingCallbackIntegrationEvent
import java.lang.reflect.Modifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MediaProcessingCallbackContractTest {
    @Test
    fun `callback integration event keeps only external task id and status`() {
        val propertyNames =
            MediaProcessingCallbackIntegrationEvent::class.java.declaredFields
                .filterNot { it.isSynthetic || Modifier.isStatic(it.modifiers) }
                .map { it.name }
                .sorted()

        assertEquals(listOf("externalTaskId", "status"), propertyNames)
    }
}
