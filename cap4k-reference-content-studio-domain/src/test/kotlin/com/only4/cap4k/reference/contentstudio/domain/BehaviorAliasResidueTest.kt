package com.only4.cap4k.reference.contentstudio.domain

import kotlin.io.path.Path
import kotlin.io.path.readText
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class BehaviorAliasResidueTest {

    @Test
    fun `behavior layer no longer keeps status value alias residue`() {
        val contentBehavior =
            sourceText(
                "cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/ContentBehavior.kt",
            )
        val mediaProcessingBehavior =
            sourceText(
                "cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/MediaProcessingTaskBehavior.kt",
            )
        val publicationEligibility =
            sourceText(
                "cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/services/PublicationEligibilityDomainService.kt",
            )

        assertFalse(contentBehavior.contains("reviewStatusValue"))
        assertFalse(contentBehavior.contains("contentStatusValue"))
        assertFalse(mediaProcessingBehavior.contains("processingStatusValue"))
        assertFalse(publicationEligibility.contains("reviewStatusValue"))
        assertFalse(publicationEligibility.contains("processingStatusValue"))
    }

    private fun sourceText(relativePath: String): String =
        Path(projectRoot(), relativePath).readText()

    private fun projectRoot(): String {
        var current = Path(System.getProperty("user.dir")).toAbsolutePath()
        while (current.parent != null) {
            if (current.resolve("cap4k-reference-content-studio-domain").toFile().exists()) {
                return current.toString()
            }
            current = current.parent
        }
        error("Could not locate project root from ${System.getProperty("user.dir")}")
    }
}
