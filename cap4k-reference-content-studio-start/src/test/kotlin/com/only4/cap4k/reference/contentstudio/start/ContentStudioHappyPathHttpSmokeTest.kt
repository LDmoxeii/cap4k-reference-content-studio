package com.only4.cap4k.reference.contentstudio.start

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.StartMediaProcessingCmd
import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.inbound.media.processing.MediaProcessingCallbackIntegrationEvent
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
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
import org.springframework.transaction.support.TransactionTemplate

@ContentStudioSpringBootTest
class ContentStudioHappyPathHttpSmokeTest(
    @param:Autowired private val restTemplate: TestRestTemplate,
    @param:Autowired private val objectMapper: ObjectMapper,
    @param:Autowired private val jdbcTemplate: JdbcTemplate,
    @param:Autowired private val transactionTemplate: TransactionTemplate,
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
        assertThat(content.required("releasePolicy").asText()).isEqualTo("IMMEDIATE")
        assertThat(content.required("mediaReadyAt").isNull).isFalse()
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

    @Test
    fun `start command no-ops for already submitted media processing task`() {
        val contentId = UUID.fromString(ContentId.new().toString())
        val mediaSourceKey = "media/submitted-idempotency-${UUID.randomUUID()}.mp4"
        val submittedExternalTaskId = "submitted-before-reapproval-$contentId"
        insertContentWithSubmittedMediaTask(
            contentId = contentId,
            mediaSourceKey = mediaSourceKey,
            externalTaskId = submittedExternalTaskId,
        )

        transactionTemplate.execute<StartMediaProcessingCmd.Response> {
            Mediator.cmd.send(StartMediaProcessingCmd.Request(ContentId.parse(contentId.toString()), mediaSourceKey))
        }

        assertThat(mediaExternalTaskId(contentId)).isEqualTo(submittedExternalTaskId)
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
        val reviewerId = ContentId.new().toString()
        val approveResponse =
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

    private fun insertContentWithSubmittedMediaTask(contentId: UUID, mediaSourceKey: String, externalTaskId: String) {
        val taskId = UUID.randomUUID()
        val now = LocalDateTime.now()
        jdbcTemplate.update(
            """
            insert into content (
                id, title, body, media_source_key, review_status, content_status, release_policy,
                reviewer_id, reviewed_at, published_at, db_created_at, db_updated_at
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            contentId,
            "Submitted media idempotency",
            "Start command must not restart an already submitted external job",
            mediaSourceKey,
            ReviewStatus.APPROVED.ordinal,
            ContentStatus.DRAFT.ordinal,
            ReleasePolicy.IMMEDIATE.ordinal,
            UUID.fromString(ContentId.new().toString()),
            now,
            null,
            now,
            now,
        )
        jdbcTemplate.update(
            """
            insert into media_processing_task (
                id, content_id, external_task_id, processing_status, result_snapshot, db_created_at, db_updated_at
            ) values (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            taskId,
            contentId,
            externalTaskId,
            MediaProcessingStatus.SUBMITTED.ordinal,
            null,
            now,
            now,
        )
    }

    private fun mediaExternalTaskId(contentId: UUID): String? =
        jdbcTemplate.queryForObject(
            """
            select external_task_id
            from media_processing_task
            where content_id = ?
            """.trimIndent(),
            String::class.java,
            contentId,
        )

    private fun sendMediaSucceededCallback(externalTaskId: String) {
        val callbackPayload =
            """
            {
              "externalTaskId": "$externalTaskId",
              "status": "COMPLETED",
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
