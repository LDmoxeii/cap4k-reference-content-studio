package com.only4.cap4k.reference.contentstudio.adapter

import com.only4.cap4k.reference.contentstudio.adapter.application.queries.content.read.GetContentDetailQryHandler
import com.only4.cap4k.reference.contentstudio.adapter.application.queries.content.read.GetMediaProcessingStatusQryHandler
import com.only4.cap4k.reference.contentstudio.adapter.application.queries.media.processing.ListSubmittedMediaProcessingTasksForPollingQryHandler
import kotlin.io.path.Path
import kotlin.io.path.readText
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class QueryHandlerTacticalContractTest {

    @Test
    fun `query handlers keep generated skeleton shape with no constructor dependencies`() {
        assertEquals(emptyList<Class<*>>(), GetContentDetailQryHandler::class.java.constructors.single().parameterTypes.toList())
        assertEquals(
            emptyList<Class<*>>(),
            GetMediaProcessingStatusQryHandler::class.java.constructors.single().parameterTypes.toList(),
        )
        assertEquals(
            emptyList<Class<*>>(),
            ListSubmittedMediaProcessingTasksForPollingQryHandler::class.java.constructors.single().parameterTypes.toList(),
        )
    }

    @Test
    fun `query handlers read through mediator repositories with persist false`() {
        val contentHandlerSource =
            sourceText(
                "cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/application/queries/content/read/GetContentDetailQryHandler.kt",
            )
        assertTrue(contentHandlerSource.contains("Mediator.repositories.findOne("))
        assertTrue(contentHandlerSource.contains("persist = false"))

        val mediaHandlerSource =
            sourceText(
                "cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/application/queries/content/read/GetMediaProcessingStatusQryHandler.kt",
            )
        assertTrue(mediaHandlerSource.contains("Mediator.repositories.findFirst("))
        assertTrue(mediaHandlerSource.contains("persist = false"))

        val pollingHandlerSource =
            sourceText(
                "cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/application/queries/media/processing/ListSubmittedMediaProcessingTasksForPollingQryHandler.kt",
            )
        assertTrue(pollingHandlerSource.contains("Mediator.repositories.find("))
        assertTrue(pollingHandlerSource.contains("persist = false"))
    }

    @Test
    fun `adapter query surface no longer keeps legacy persistence path residue`() {
        assertFalse(classExists("com.only4.cap4k.reference.contentstudio.adapter.application.queries.GetContentDetailQryHandler"))
        assertFalse(classExists("com.only4.cap4k.reference.contentstudio.adapter.application.queries.GetCurrentProcessingStatusQryHandler"))
        assertFalse(classExists("com.only4.cap4k.reference.contentstudio.adapter.persistence.ContentPersistenceAdapter"))
        assertFalse(classExists("com.only4.cap4k.reference.contentstudio.adapter.persistence.MediaProcessingTaskPersistenceAdapter"))
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

    private fun classExists(className: String): Boolean =
        try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
}
