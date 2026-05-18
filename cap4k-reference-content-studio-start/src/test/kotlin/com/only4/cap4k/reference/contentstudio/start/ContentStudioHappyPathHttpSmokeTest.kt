package com.only4.cap4k.reference.contentstudio.start

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.inbound.media.processing.MediaProcessingCallbackIntegrationEvent
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
class ContentStudioHappyPathHttpSmokeTest(
    @param:Autowired private val restTemplate: TestRestTemplate,
    @param:Autowired private val objectMapper: ObjectMapper,
) {

    @Test
    fun `http happy path runs from draft creation through callback-driven publish`() {
        val contentId = createImmediateContent(
            title = "Strict HTTP path",
            body = "Exercise the real runtime stack",
            mediaSourceKey = "media/http-smoke-${UUID.randomUUID()}.mp4",
        )

        submitContentForReview(contentId)
        approveContent(contentId)
        val externalTaskId = waitForSubmittedMediaExternalTaskId(contentId)
        sendMediaSucceededCallback(externalTaskId)

        val content = waitForJson(Duration.ofSeconds(5)) {
            val response = restTemplate.getForEntity("/contents/$contentId", String::class.java)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            json(response.body).takeIf { root ->
                root.path("contentStatus").asText() == "PUBLISHED"
            }
        }
        assertThat(content.required("reviewStatus").asText()).isEqualTo("APPROVED")
        assertThat(content.required("contentStatus").asText()).isEqualTo("PUBLISHED")
        assertThat(content.required("publishedAt").isNull).isFalse()

        val succeededTask = waitForJson(Duration.ofSeconds(5)) {
            val response = restTemplate.getForEntity("/media-processing/$contentId", String::class.java)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            json(response.body).takeIf { root ->
                root.path("task").isObject &&
                    root.path("task").path("processingStatus").asText() == "SUCCEEDED"
            }
        }.required("task")
        assertThat(succeededTask.required("externalTaskId").asText()).isEqualTo(externalTaskId)
    }

    @Test
    fun `immediate publication recovers when media succeeds during re-review`() {
        val contentId = createImmediateContent(
            title = "Immediate re-review race",
            body = "Media can finish while review is pending again",
            mediaSourceKey = "media/immediate-rereview-${UUID.randomUUID()}.mp4",
        )

        submitContentForReview(contentId)
        approveContent(contentId)
        val externalTaskId = waitForSubmittedMediaExternalTaskId(contentId)

        submitContentForReview(contentId)
        assertContentState(contentId, reviewStatus = "PENDING", contentStatus = "DRAFT")

        sendMediaSucceededCallback(externalTaskId)
        assertContentState(contentId, reviewStatus = "PENDING", contentStatus = "DRAFT")

        approveContent(contentId)
        assertPublishedContent(contentId)
    }

    private fun createImmediateContent(title: String, body: String, mediaSourceKey: String): String {
        val createResponse =
            restTemplate.postForEntity(
                "/contents",
                jsonRequest(
                    """
                    {
                      "title": "$title",
                      "body": "$body",
                      "mediaSourceKey": "$mediaSourceKey"
                    }
                    """.trimIndent()
                ),
                String::class.java,
            )

        assertThat(createResponse.statusCode).isEqualTo(HttpStatus.OK)
        return json(createResponse.body).required("contentId").asText()
    }

    private fun submitContentForReview(contentId: String) {
        val submitResponse =
            restTemplate.postForEntity(
                "/contents/$contentId/submit-review",
                HttpEntity.EMPTY,
                String::class.java,
            )
        assertThat(submitResponse.statusCode).isEqualTo(HttpStatus.OK)
    }

    private fun approveContent(contentId: String) {
        val approveResponse =
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
            )
        assertThat(approveResponse.statusCode).isEqualTo(HttpStatus.OK)
    }

    private fun waitForSubmittedMediaExternalTaskId(contentId: String): String {
        val submittedTask = waitForJson(Duration.ofSeconds(5)) {
            val response = restTemplate.getForEntity("/media-processing/$contentId", String::class.java)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            json(response.body).takeIf { root ->
                root.path("task").isObject &&
                    root.path("task").path("processingStatus").asText() == "SUBMITTED"
            }
        }.required("task")
        return submittedTask.required("externalTaskId").asText()
    }

    private fun sendMediaSucceededCallback(externalTaskId: String) {
        val callbackPayload =
            """
            {
              "externalTaskId": "$externalTaskId",
              "status": "SUCCEEDED",
              "assetSha256": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
              "assetLocation": "s3://content-studio/assets/$externalTaskId.mp4",
              "completedAt": "2026-05-11T10:15:30"
            }
            """.trimIndent()

        val callbackResponse =
            restTemplate.postForEntity(
                "/cap4k/integration-event/http/consume?event=${MediaProcessingCallbackIntegrationEvent.EVENT_NAME}&uuid=${UUID.randomUUID()}",
                jsonRequest(callbackPayload),
                String::class.java,
            )
        assertThat(callbackResponse.statusCode).isEqualTo(HttpStatus.OK)
    }

    private fun assertPublishedContent(contentId: String) {
        val content = waitForJson(Duration.ofSeconds(5)) {
            val response = restTemplate.getForEntity("/contents/$contentId", String::class.java)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            json(response.body).takeIf { root ->
                root.path("contentStatus").asText() == "PUBLISHED"
            }
        }
        assertThat(content.required("contentStatus").asText()).isEqualTo("PUBLISHED")
        assertThat(content.required("publishedAt").isNull).isFalse()
    }

    private fun assertContentState(contentId: String, reviewStatus: String, contentStatus: String) {
        val response = restTemplate.getForEntity("/contents/$contentId", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val content = json(response.body)
        assertThat(content.required("reviewStatus").asText()).isEqualTo(reviewStatus)
        assertThat(content.required("contentStatus").asText()).isEqualTo(contentStatus)
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
