package com.only4.cap4k.reference.contentstudio.adapter

import com.only4.cap4k.reference.contentstudio.adapter.integration.MediaProcessingCallbackIntegrationEventSubscriber
import kotlin.io.path.Path
import kotlin.io.path.readText
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MediaProcessingCallbackIntegrationEventSubscriberContractTest {

    @Test
    fun `callback subscriber keeps generated-skeleton style no arg shape`() {
        assertEquals(
            emptyList<Class<*>>(),
            MediaProcessingCallbackIntegrationEventSubscriber::class.java.constructors.single().parameterTypes.toList(),
        )
    }

    @Test
    fun `callback subscriber routes through mediator command path without transition residue`() {
        val source =
            sourceText(
                "cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/integration/MediaProcessingCallbackIntegrationEventSubscriber.kt",
            )

        assertTrue(source.contains("Mediator.cmd.send("))
        assertTrue(source.contains("MarkMediaProcessingSucceededCmd.Request("))
        assertTrue(source.contains("SUCCEEDED_STATUS"))
        assertFalse(source.contains("RequestSupervisor"))
        assertFalse(source.contains("TransitionSurface"))
        assertFalse(source.contains("MediaProcessingSucceededTransitionSurface"))
    }

    private fun sourceText(relativePath: String): String =
        Path(projectRoot(), relativePath).readText()

    private fun projectRoot(): String {
        var current = Path(System.getProperty("user.dir")).toAbsolutePath()
        while (current.parent != null) {
            if (current.resolve("cap4k-reference-content-studio-adapter").toFile().exists()) {
                return current.toString()
            }
            current = current.parent
        }
        error("Could not locate project root from ${System.getProperty("user.dir")}")
    }
}
