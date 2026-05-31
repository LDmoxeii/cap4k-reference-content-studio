package com.only4.cap4k.reference.contentstudio.start

import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.file.Files
import java.nio.file.Paths
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PublishContentCommandContractTest {

    @Test
    fun `publish content request does not expose caller supplied policy gate`() {
        val designPath =
            sequenceOf(
                Paths.get("design", "design.json"),
                Paths.get("..", "design", "design.json"),
            ).first(Files::exists)
        val design = ObjectMapper().readTree(designPath.toFile())
        val publishContentCommand =
            design.first { node ->
                node.path("tag").asText() == "command" &&
                    node.path("package").asText() == "content.workflow" &&
                    node.path("name").asText() == "PublishContent"
            }

        val requestFieldNames = publishContentCommand.required("fields").map { it.required("name").asText() }

        assertThat(requestFieldNames).doesNotContain("policyGateSatisfied")
    }
}
