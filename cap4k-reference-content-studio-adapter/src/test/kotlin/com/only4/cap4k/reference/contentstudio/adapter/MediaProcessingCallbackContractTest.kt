package com.only4.cap4k.reference.contentstudio.adapter

import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.MediaProcessingCallbackIntegrationEvent
import java.lang.reflect.Modifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MediaProcessingCallbackContractTest {
    @Test
    fun `callback integration event includes processing result fields`() {
        val propertyNames =
            MediaProcessingCallbackIntegrationEvent::class.java.declaredFields
                .filterNot { it.isSynthetic || Modifier.isStatic(it.modifiers) }
                .map { it.name }
                .sorted()

        assertEquals(
            listOf("assetLocation", "assetSha256", "completedAt", "externalTaskId", "status"),
            propertyNames,
        )
    }
}
