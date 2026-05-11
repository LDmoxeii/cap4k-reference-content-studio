package com.only4.cap4k.reference.contentstudio.start

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.MediaProcessingCallbackIntegrationEvent
import java.time.Duration
import java.time.LocalDateTime
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
class ContentStudioAdvancedReleaseReadinessHttpSmokeTest(
    @param:Autowired private val restTemplate: TestRestTemplate,
    @param:Autowired private val objectMapper: ObjectMapper,
) {

    @Test
    fun `advanced gated path waits for release readiness before publishing`() {
        val releaseWindowOpensAt = LocalDateTime.now().minusMinutes(5)
        val releaseWindowClosesAt = LocalDateTime.now().plusDays(1)
        val createResponse =
            restTemplate.postForEntity(
                "/advanced/contents/gated",
                jsonRequest(
                    """
                    {
                      "title": "Gated HTTP path",
                      "body": "Exercise the gated release readiness path",
                      "mediaSourceKey": "media/gated-http-smoke.mp4",
                      "releaseWindowOpensAt": "$releaseWindowOpensAt",
                      "releaseWindowClosesAt": "$releaseWindowClosesAt"
                    }
                    """.trimIndent()
                ),
                String::class.java,
            )

        assertThat(createResponse.statusCode).isEqualTo(HttpStatus.OK)
        val contentId = json(createResponse.body).required("contentId").asText()

        val submitResponse =
            restTemplate.postForEntity(
                "/contents/$contentId/submit-review",
                HttpEntity.EMPTY,
                String::class.java,
            )
        assertThat(submitResponse.statusCode).isEqualTo(HttpStatus.OK)

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

        val submittedTask = waitForJson(Duration.ofSeconds(5)) {
            val response = restTemplate.getForEntity("/media-processing/$contentId", String::class.java)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            json(response.body).takeIf { root ->
                root.path("task").isObject &&
                    root.path("task").path("processingStatus").asText() == "SUBMITTED"
            }
        }.required("task")
        val externalTaskId = submittedTask.required("externalTaskId").asText()

        val callbackResponse =
            restTemplate.postForEntity(
                "/cap4k/integration-event/http/consume?event=${MediaProcessingCallbackIntegrationEvent.EVENT_NAME}&uuid=${UUID.randomUUID()}",
                jsonRequest(
                    """
                    {
                      "externalTaskId": "$externalTaskId",
                      "status": "${MediaProcessingCallbackIntegrationEvent.SUCCEEDED_STATUS}",
                      "assetSha256": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                      "assetLocation": "s3://content-studio/assets/$externalTaskId.mp4",
                      "completedAt": "2026-05-11T10:15:30"
                    }
                    """.trimIndent()
                ),
                String::class.java,
            )
        assertThat(callbackResponse.statusCode).isEqualTo(HttpStatus.OK)

        waitForJson(Duration.ofSeconds(5)) {
            val response = restTemplate.getForEntity("/media-processing/$contentId", String::class.java)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            json(response.body).takeIf { root ->
                root.path("task").isObject &&
                    root.path("task").path("processingStatus").asText() == "SUCCEEDED"
            }
        }

        val contentBeforeReadiness = restTemplate.getForEntity("/contents/$contentId", String::class.java)
        assertThat(contentBeforeReadiness.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(json(contentBeforeReadiness.body).required("contentStatus").asText()).isNotEqualTo("PUBLISHED")

        assertThat(
            restTemplate.postForEntity(
                "/advanced/contents/$contentId/release-readiness/copyright-pass",
                HttpEntity.EMPTY,
                String::class.java,
            ).statusCode
        ).isEqualTo(HttpStatus.OK)
        assertThat(
            restTemplate.postForEntity(
                "/advanced/contents/$contentId/release-readiness/manual-confirm",
                HttpEntity.EMPTY,
                String::class.java,
            ).statusCode
        ).isEqualTo(HttpStatus.OK)
        assertThat(
            restTemplate.postForEntity(
                "/advanced/contents/$contentId/release-readiness/complete",
                HttpEntity.EMPTY,
                String::class.java,
            ).statusCode
        ).isEqualTo(HttpStatus.OK)

        val contentAfterReadiness = waitForJson(Duration.ofSeconds(5)) {
            val response = restTemplate.getForEntity("/contents/$contentId", String::class.java)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            json(response.body).takeIf { root ->
                root.path("contentStatus").asText() == "PUBLISHED"
            }
        }
        assertThat(contentAfterReadiness.required("reviewStatus").asText()).isEqualTo("APPROVED")
        assertThat(contentAfterReadiness.required("publishedAt").isNull).isFalse()
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
