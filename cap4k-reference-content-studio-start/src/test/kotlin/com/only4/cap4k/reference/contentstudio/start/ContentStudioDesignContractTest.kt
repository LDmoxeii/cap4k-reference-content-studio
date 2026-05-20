package com.only4.cap4k.reference.contentstudio.start

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.outbound.content.ContentPublishedIntegrationEvent
import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.inbound.media.processing.MediaProcessingCallbackIntegrationEvent
import java.nio.file.Files
import java.nio.file.Paths
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.reflect.full.primaryConstructor

class ContentStudioDesignContractTest {

    private val design: JsonNode by lazy {
        val designPath =
            sequenceOf(
                Paths.get("design", "design.json"),
                Paths.get("..", "design", "design.json"),
            ).first(Files::exists)
        ObjectMapper().readTree(designPath.toFile())
    }

    @Test
    fun `content requires media processing domain event is modeled as content business fact`() {
        val event = requireDesignEntry("domain_event", "ContentRequiresMediaProcessing")

        assertThat(event.required("package").asText()).isEqualTo("content")
        assertThat(event.requestFieldNames()).containsExactly("contentId", "mediaSourceKey")
    }

    @Test
    fun `media processing callback inbound event uses completed integration contract`() {
        val event = requireDesignEntry("integration_event", "MediaProcessingCallback")

        assertThat(event.required("role").asText()).isEqualTo("inbound")
        assertThat(event.required("eventName").asText())
            .isEqualTo("cap4k.reference.contentstudio.media-processing.completed")
        assertThat(event.requestFieldNames())
            .contains("externalTaskId", "status", "assetSha256", "assetLocation", "completedAt")
        assertThat(event.requestField("assetSha256").path("nullable").asBoolean(false)).isTrue()
        assertThat(event.requestField("assetLocation").path("nullable").asBoolean(false)).isTrue()
    }

    @Test
    fun `generated media processing callback integration event uses completed event name`() {
        assertThat(MediaProcessingCallbackIntegrationEvent.EVENT_NAME)
            .isEqualTo("cap4k.reference.contentstudio.media-processing.completed")
    }

    @Test
    fun `generated media processing callback integration event keeps nullable asset fields`() {
        val constructorParameters =
            requireNotNull(MediaProcessingCallbackIntegrationEvent::class.primaryConstructor)
                .parameters
                .associateBy { parameter -> parameter.name }

        assertThat(constructorParameters.getValue("assetSha256").type.isMarkedNullable).isTrue()
        assertThat(constructorParameters.getValue("assetLocation").type.isMarkedNullable).isTrue()
    }

    @Test
    fun `content published outbound integration event exposes publication fact`() {
        val event = requireDesignEntry("integration_event", "ContentPublished")

        assertThat(event.required("role").asText()).isEqualTo("outbound")
        assertThat(event.required("eventName").asText())
            .isEqualTo("cap4k.reference.contentstudio.content.published")
        assertThat(event.requestFieldNames()).containsExactly("contentId", "releasePolicy", "publishedAt")
    }

    @Test
    fun `generated content published outbound integration event uses stable event name`() {
        assertThat(ContentPublishedIntegrationEvent.EVENT_NAME)
            .isEqualTo("cap4k.reference.contentstudio.content.published")
    }

    private fun requireDesignEntry(tag: String, name: String): JsonNode {
        val entry =
            design.firstOrNull { node ->
                node.path("tag").asText() == tag &&
                    node.path("name").asText() == name
            }

        assertThat(entry)
            .describedAs("design entry tag=%s name=%s", tag, name)
            .isNotNull
        return entry!!
    }

    private fun JsonNode.requestFieldNames(): List<String> =
        required("requestFields").map { field -> field.required("name").asText() }

    private fun JsonNode.requestField(name: String): JsonNode {
        val field = required("requestFields").firstOrNull { it.path("name").asText() == name }

        assertThat(field)
            .describedAs("request field name=%s", name)
            .isNotNull
        return field!!
    }
}
