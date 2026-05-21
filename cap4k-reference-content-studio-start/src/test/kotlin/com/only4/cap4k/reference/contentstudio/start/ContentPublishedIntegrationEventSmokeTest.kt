package com.only4.cap4k.reference.contentstudio.start

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.inbound.media.processing.MediaProcessingCallbackIntegrationEvent
import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.outbound.content.ContentPublishedIntegrationEvent
import java.time.Duration
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate

@ContentStudioSpringBootTest
class ContentPublishedIntegrationEventSmokeTest(
    @param:Autowired private val restTemplate: TestRestTemplate,
    @param:Autowired private val objectMapper: ObjectMapper,
    @param:Autowired private val jdbcTemplate: JdbcTemplate,
) {

    @Test
    fun `published content emits outbound integration event`() {
        val contentId = createAndApproveContent()
        val externalTaskId = waitForSubmittedMediaExternalTaskId(contentId)

        sendSucceededCallback(externalTaskId)
        waitForPublishedContent(contentId)

        val event = waitForPersistedContentPublishedEvent(contentId)
        assertThat(event.eventType).isEqualTo(ContentPublishedIntegrationEvent.EVENT_NAME)
        assertThat(event.dataType).isEqualTo(ContentPublishedIntegrationEvent::class.qualifiedName)
        assertThat(event.data)
            .contains(contentId)
            .contains("\"releasePolicy\":\"IMMEDIATE\"")
    }

    private fun createAndApproveContent(): String {
        val createResponse =
            restTemplate.postForEntity(
                "/contents",
                jsonRequest(
                    """
                    {
                      "title": "Outbound integration event",
                      "body": "Focused coverage for published content emission",
                      "mediaSourceKey": "media/outbound-${UUID.randomUUID()}.mp4"
                    }
                    """.trimIndent()
                ),
                String::class.java,
            )
        assertThat(createResponse.statusCode).isEqualTo(HttpStatus.OK)
        val contentId = json(createResponse.body).required("contentId").asText()

        assertThat(
            restTemplate.postForEntity(
                "/contents/$contentId/submit-review",
                HttpEntity.EMPTY,
                String::class.java,
            ).statusCode
        ).isEqualTo(HttpStatus.OK)

        assertThat(
            restTemplate.postForEntity(
                "/contents/$contentId/approve",
                jsonRequest(
                    """
                    {
                      "reviewerId": "11111111-1111-1111-1111-111111111111"
                    }
                    """.trimIndent()
                ),
                String::class.java,
            ).statusCode
        ).isEqualTo(HttpStatus.OK)

        return contentId
    }

    private fun waitForSubmittedMediaExternalTaskId(contentId: String): String =
        waitForJson(Duration.ofSeconds(5)) {
            val response = restTemplate.getForEntity("/media-processing/$contentId", String::class.java)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            json(response.body).takeIf { root ->
                root.path("task").isObject &&
                    root.path("task").path("processingStatus").asText() == "SUBMITTED"
            }
        }.required("task").required("externalTaskId").asText()

    private fun sendSucceededCallback(externalTaskId: String) {
        val payload =
            """
            {
              "externalTaskId": "$externalTaskId",
              "status": "COMPLETED",
              "assetSha256": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
              "assetLocation": "s3://content-studio/assets/$externalTaskId.mp4",
              "completedAt": "2026-05-11T10:15:30"
            }
            """.trimIndent()

        val response =
            restTemplate.postForEntity(
                "/cap4k/integration-event/http/consume?event=${MediaProcessingCallbackIntegrationEvent.EVENT_NAME}&uuid=${UUID.randomUUID()}",
                jsonRequest(payload),
                String::class.java,
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    private fun waitForPublishedContent(contentId: String): JsonNode =
        waitForJson(Duration.ofSeconds(5)) {
            val response = restTemplate.getForEntity("/contents/$contentId", String::class.java)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            json(response.body).takeIf { root ->
                root.path("contentStatus").asText() == "PUBLISHED"
            }
        }

    private fun waitForPersistedContentPublishedEvent(contentId: String): PersistedEventRow =
        waitForValue(Duration.ofSeconds(5)) {
            contentPublishedEvents(contentId).singleOrNull()
        } ?: error("Timed out waiting for persisted ${ContentPublishedIntegrationEvent.EVENT_NAME} for contentId=$contentId")

    private fun contentPublishedEvents(contentId: String): List<PersistedEventRow> =
        jdbcTemplate.query(
            """
            select event_type, data_type, data
            from __event
            where event_type = ? and data_type = ? and data like ?
            union all
            select event_type, data_type, data
            from __archived_event
            where event_type = ? and data_type = ? and data like ?
            """.trimIndent(),
            { rs, _ ->
                PersistedEventRow(
                    eventType = rs.getString("event_type"),
                    dataType = rs.getString("data_type"),
                    data = rs.getString("data"),
                )
            },
            ContentPublishedIntegrationEvent.EVENT_NAME,
            ContentPublishedIntegrationEvent::class.qualifiedName,
            "%$contentId%",
            ContentPublishedIntegrationEvent.EVENT_NAME,
            ContentPublishedIntegrationEvent::class.qualifiedName,
            "%$contentId%",
        )

    private fun jsonRequest(body: String): HttpEntity<String> =
        HttpEntity(
            body,
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                accept = listOf(MediaType.APPLICATION_JSON)
            },
        )

    private fun json(body: String?): JsonNode = objectMapper.readTree(body)

    private fun waitForJson(timeout: Duration, fetch: () -> JsonNode?): JsonNode {
        val deadlineNanos = System.nanoTime() + timeout.toNanos()
        var latest: JsonNode? = null
        while (System.nanoTime() < deadlineNanos) {
            latest = fetch()
            if (latest != null) {
                return latest
            }
            Thread.sleep(100)
        }
        return checkNotNull(latest) {
            "Timed out after $timeout while waiting for HTTP state."
        }
    }

    private fun <T : Any> waitForValue(timeout: Duration, fetch: () -> T?): T? {
        val deadlineNanos = System.nanoTime() + timeout.toNanos()
        var latest: T? = null
        while (System.nanoTime() < deadlineNanos) {
            latest = fetch()
            if (latest != null) {
                return latest
            }
            Thread.sleep(100)
        }
        return latest
    }

    private data class PersistedEventRow(
        val eventType: String,
        val dataType: String,
        val data: String,
    )
}
