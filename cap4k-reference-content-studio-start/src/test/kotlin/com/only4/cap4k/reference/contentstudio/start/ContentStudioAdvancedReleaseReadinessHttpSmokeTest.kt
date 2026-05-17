package com.only4.cap4k.reference.contentstudio.start

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.inbound.media.processing.MediaProcessingCallbackIntegrationEvent
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
import org.springframework.jdbc.core.JdbcTemplate

@ContentStudioSpringBootTest
class ContentStudioAdvancedReleaseReadinessHttpSmokeTest(
    @param:Autowired private val restTemplate: TestRestTemplate,
    @param:Autowired private val objectMapper: ObjectMapper,
    @param:Autowired private val jdbcTemplate: JdbcTemplate,
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
                      "status": "SUCCEEDED",
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
        assertThat(tableExists("__saga")).isTrue()
        assertThat(tableExists("__saga_process")).isTrue()
        assertThat(sagaCount(contentId)).isZero()

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

        waitForSagaCount(Duration.ofSeconds(5), contentId)

        val contentAfterReadiness = waitForJson(Duration.ofSeconds(5)) {
            val response = restTemplate.getForEntity("/contents/$contentId", String::class.java)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            json(response.body).takeIf { root ->
                root.path("contentStatus").asText() == "PUBLISHED"
            }
        }
        assertThat(contentAfterReadiness.required("reviewStatus").asText()).isEqualTo("APPROVED")
        assertThat(contentAfterReadiness.required("publishedAt").isNull).isFalse()
        assertThat(sagaProcessCodes(contentId))
            .contains("complete-release-readiness", "publish-content")
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

    private fun tableExists(tableName: String): Boolean =
        jdbcTemplate.queryForObject(
            """
            select count(*)
            from INFORMATION_SCHEMA.TABLES
            where TABLE_SCHEMA = 'PUBLIC'
              and TABLE_NAME = ?
            """.trimIndent(),
            Int::class.java,
            tableName,
        )!! > 0

    private fun sagaCount(contentId: String): Int =
        jdbcTemplate.queryForObject(
            """
            select count(*)
            from __saga saga
            join publication_release_readiness readiness on readiness.release_saga_id = saga.saga_uuid
            where readiness.content_id = ?
            """.trimIndent(),
            Int::class.java,
            UUID.fromString(contentId),
        )!!

    private fun waitForSagaCount(timeout: Duration, contentId: String): Int {
        val deadlineNanos = System.nanoTime() + timeout.toNanos()
        var latest = 0
        while (System.nanoTime() < deadlineNanos) {
            latest = sagaCount(contentId)
            if (latest > 0) {
                return latest
            }
            Thread.sleep(100)
        }
        return latest.also {
            assertThat(it).isGreaterThan(0)
        }
    }

    private fun sagaProcessCodes(contentId: String): List<String> =
        jdbcTemplate.queryForList(
            """
            select process.process_code
            from __saga_process process
            join __saga saga on saga.id = process.saga_id
            join publication_release_readiness readiness on readiness.release_saga_id = saga.saga_uuid
            where readiness.content_id = ?
            order by process.id
            """.trimIndent(),
            String::class.java,
            UUID.fromString(contentId),
        )

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
