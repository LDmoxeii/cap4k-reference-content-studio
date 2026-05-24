package com.only4.cap4k.reference.contentstudio.start

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.inbound.media.processing.MediaProcessingCallbackIntegrationEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
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

@ContentStudioSpringBootTest
class MediaProcessingCallbackIntegrationEventSmokeTest(
    @param:Autowired private val restTemplate: TestRestTemplate,
    @param:Autowired private val objectMapper: ObjectMapper,
) {

    @Test
    fun `succeeded callback is accepted as successful media processing`() {
        val contentId = createAndApproveContent("succeeded-callback")
        val externalTaskId = waitForSubmittedMediaExternalTaskId(contentId)

        sendCallback(
            """
            {
              "externalTaskId": "$externalTaskId",
              "status": "SUCCEEDED",
              "assetSha256": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
              "assetLocation": "s3://content-studio/assets/$externalTaskId.mp4",
              "completedAt": "2026-05-11T10:15:30"
            }
            """.trimIndent()
        )

        val task = waitForMediaTask(contentId, processingStatus = "SUCCEEDED")
        assertThat(task.required("externalTaskId").asText()).isEqualTo(externalTaskId)
    }

    @Test
    fun `failed callback with null assets is ignored before command routing`() {
        val contentId = createAndApproveContent("failed-callback")
        val externalTaskId = waitForSubmittedMediaExternalTaskId(contentId)

        sendCallback(
            """
            {
              "externalTaskId": "$externalTaskId",
              "status": "FAILED",
              "assetSha256": null,
              "assetLocation": null,
              "completedAt": "2026-05-11T10:15:30"
            }
            """.trimIndent()
        )

        val task = mediaTask(contentId)
        assertThat(task.required("processingStatus").asText()).isEqualTo("SUBMITTED")
        val content = content(contentId)
        assertThat(content.required("contentStatus").asText()).isEqualTo("DRAFT")
        assertThat(content.required("mediaReadyAt").isNull).isTrue()
    }

    private fun createAndApproveContent(scenario: String): String {
        val createResponse =
            restTemplate.postForEntity(
                "/contents",
                jsonRequest(
                    """
                    {
                      "title": "Callback guard $scenario",
                      "body": "Focused callback status guard coverage",
                      "mediaSourceKey": "media/$scenario-${UUID.randomUUID()}.mp4"
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

        val reviewerId = ContentId.new().toString()
        assertThat(
            restTemplate.postForEntity(
                "/contents/$contentId/approve",
                jsonRequest(
                    """
                    {
                      "reviewerId": "$reviewerId"
                    }
                    """.trimIndent()
                ),
                String::class.java,
            ).statusCode
        ).isEqualTo(HttpStatus.OK)

        return contentId
    }

    private fun waitForSubmittedMediaExternalTaskId(contentId: String): String =
        waitForMediaTask(contentId, processingStatus = "SUBMITTED")
            .required("externalTaskId")
            .asText()

    private fun waitForMediaTask(contentId: String, processingStatus: String): JsonNode =
        waitForJson(Duration.ofSeconds(5)) {
            mediaTaskOrNull(contentId)?.takeIf { task ->
                task.required("processingStatus").asText() == processingStatus
            }
        }

    private fun mediaTask(contentId: String): JsonNode {
        val response = restTemplate.getForEntity("/media-processing/$contentId", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        return json(response.body).required("task")
    }

    private fun mediaTaskOrNull(contentId: String): JsonNode? {
        val response = restTemplate.getForEntity("/media-processing/$contentId", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val task = json(response.body).path("task")
        return task.takeIf(JsonNode::isObject)
    }

    private fun content(contentId: String): JsonNode {
        val response = restTemplate.getForEntity("/contents/$contentId", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        return json(response.body)
    }

    private fun sendCallback(payload: String) {
        val response =
            restTemplate.postForEntity(
                "/cap4k/integration-event/http/consume?event=${MediaProcessingCallbackIntegrationEvent.EVENT_NAME}&uuid=${UUID.randomUUID()}",
                jsonRequest(payload),
                String::class.java,
            )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

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
}
