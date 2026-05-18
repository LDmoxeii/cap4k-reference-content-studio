package com.only4.cap4k.reference.contentstudio.start

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.adapter.application.distributed.clients.paid.publication.FakePaidPublicationCliState
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.PublishContentCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.PublishPaidPublicationContentCmd
import com.only4.cap4k.reference.contentstudio.application.commands.paid.publication.ReserveCreatorPayoutHoldCmd
import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.inbound.media.processing.MediaProcessingCallbackIntegrationEvent
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate

@ContentStudioSpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ContentStudioPaidPublicationSagaSmokeTest(
    @param:Autowired private val restTemplate: TestRestTemplate,
    @param:Autowired private val objectMapper: ObjectMapper,
    @param:Autowired private val jdbcTemplate: JdbcTemplate,
    @param:Autowired private val fakePaidPublicationCliState: FakePaidPublicationCliState,
) {

    @AfterEach
    fun resetFakePaidPublicationCliState() {
        fakePaidPublicationCliState.setFailActivation(false)
    }

    @Test
    @Order(1)
    fun `paid publication saga publishes content and activates entitlement plan`() {
        fakePaidPublicationCliState.setFailActivation(false)

        val contentId = runPaidPublicationPath()

        val task = waitForPaidPublicationTask(Duration.ofSeconds(10), contentId) { row ->
            row.paidPublicationStatus == 2 && row.payoutHoldStatus == 1 && row.entitlementPlanStatus == 2
        }
        assertThat(task.paidPublicationStatus).isEqualTo(2)
        assertThat(task.payoutHoldStatus).isEqualTo(1)
        assertThat(task.entitlementPlanStatus).isEqualTo(2)
        assertThat(sagaProcessCodes(contentId))
            .contains(
                "reserve-payout-hold",
                "create-entitlement-plan",
                "publish-content",
                "activate-entitlement-plan",
            )
    }

    @Test
    @Order(2)
    fun `paid publication saga marks manual repair when entitlement activation fails after publish`() {
        fakePaidPublicationCliState.setFailActivation(true)

        val contentId = runPaidPublicationPath(waitForPublishedContent = false)

        val task = waitForPaidPublicationTask(Duration.ofSeconds(10), contentId) { row ->
            row.paidPublicationStatus == 4
        }
        assertThat(task.paidPublicationStatus).isEqualTo(4)
        assertThat(task.payoutHoldStatus).isEqualTo(2)
        assertThat(task.entitlementPlanStatus).isEqualTo(3)
        assertPublishedContent(contentId)
    }

    @Test
    @Order(3)
    fun `paid publication waits for review approval when media succeeds during re-review`() {
        fakePaidPublicationCliState.setFailActivation(false)

        val contentId = createPaidContent(
            title = "Paid re-review race",
            body = "Media can finish while review is pending again",
            mediaSourceKey = "media/paid-rereview-${UUID.randomUUID()}.mp4",
        )

        submitContentForReview(contentId)
        approveContent(contentId)
        val externalTaskId = waitForSubmittedMediaExternalTaskId(contentId)

        submitContentForReview(contentId)
        assertContentState(contentId, reviewStatus = "PENDING", contentStatus = "DRAFT")

        sendMediaSucceededCallback(externalTaskId)
        assertNoPaidPublicationTask(Duration.ofSeconds(2), contentId)
        assertContentState(contentId, reviewStatus = "PENDING", contentStatus = "DRAFT")

        approveContent(contentId)
        assertPublishedContent(contentId)
        waitForPaidPublicationTask(Duration.ofSeconds(10), contentId) { row ->
            row.paidPublicationStatus == 2 && row.payoutHoldStatus == 1 && row.entitlementPlanStatus == 2
        }
    }

    @Test
    @Order(4)
    fun `reserve creator payout hold no-ops when paid publication task is not saga running`() {
        val contentId = UUID.randomUUID()
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
            "Paid pending task",
            "Direct command guard coverage",
            "media/pending-task.mp4",
            1,
            0,
            2,
            null,
            now,
            null,
            now,
            now,
        )
        jdbcTemplate.update(
            """
            insert into paid_publication_task (
                id, content_id, paid_publication_status, publication_saga_id,
                payout_hold_status, payout_hold_id, entitlement_plan_status, entitlement_plan_id,
                started_at, published_at, completed_at, failed_at, failed_reason, db_created_at, db_updated_at
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            taskId,
            contentId,
            0,
            null,
            0,
            null,
            0,
            null,
            null,
            null,
            null,
            null,
            null,
            now,
            now,
        )

        val response = Mediator.cmd.send(ReserveCreatorPayoutHoldCmd.Request(taskId))

        assertThat(response.reserved).isFalse()
        val task = paidPublicationTask(contentId)
        assertThat(task?.paidPublicationStatus).isEqualTo(0)
        assertThat(task?.publicationSagaId).isNull()
        assertThat(task?.payoutHoldStatus).isEqualTo(0)
        assertThat(task?.payoutHoldId).isNull()
    }

    @Test
    @Order(5)
    fun `paid publication command rejects task that points at immediate content`() {
        val contentId = UUID.randomUUID()
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
            "Immediate content with paid task",
            "Broken invariant should not publish",
            "media/immediate-paid-task-${UUID.randomUUID()}.mp4",
            1,
            0,
            0,
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
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
            UUID.randomUUID(),
            contentId,
            "immediate-paid-task-$contentId",
            2,
            null,
            now,
            now,
        )
        jdbcTemplate.update(
            """
            insert into paid_publication_task (
                id, content_id, paid_publication_status, publication_saga_id,
                payout_hold_status, payout_hold_id, entitlement_plan_status, entitlement_plan_id,
                started_at, published_at, completed_at, failed_at, failed_reason, db_created_at, db_updated_at
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            taskId,
            contentId,
            1,
            UUID.randomUUID().toString(),
            1,
            "hold-$taskId",
            1,
            "plan-$taskId",
            now,
            null,
            null,
            null,
            null,
            now,
            now,
        )

        assertThatThrownBy {
            Mediator.cmd.send(PublishPaidPublicationContentCmd.Request(taskId))
        }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("requires paid content")

        assertThat(contentStatus(contentId)).isEqualTo(0)
    }

    @Test
    @Order(6)
    fun `generic publish command does not publish paid content directly`() {
        val contentId = UUID.randomUUID()
        val now = LocalDateTime.now()
        jdbcTemplate.update(
            """
            insert into content (
                id, title, body, media_source_key, review_status, content_status, release_policy,
                reviewer_id, reviewed_at, published_at, db_created_at, db_updated_at
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            contentId,
            "Paid direct publish",
            "Generic publish command must not bypass paid publication orchestration",
            "media/paid-direct-${UUID.randomUUID()}.mp4",
            1,
            0,
            2,
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
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
            UUID.randomUUID(),
            contentId,
            "paid-direct-$contentId",
            2,
            null,
            now,
            now,
        )

        val response = Mediator.cmd.send(PublishContentCmd.Request(contentId, now))

        assertThat(response.published).isFalse()
        assertThat(contentStatus(contentId)).isEqualTo(0)
    }

    private fun runPaidPublicationPath(waitForPublishedContent: Boolean = true): UUID {
        val contentId = createPaidContent(
            title = "Paid HTTP path",
            body = "Exercise the paid publication saga path",
            mediaSourceKey = "media/paid-http-smoke-${UUID.randomUUID()}.mp4",
        )

        submitContentForReview(contentId)
        approveContent(contentId)
        val externalTaskId = waitForSubmittedMediaExternalTaskId(contentId)
        sendMediaSucceededCallback(externalTaskId)

        if (waitForPublishedContent) {
            assertPublishedContent(contentId)
        }

        return contentId
    }

    private fun createPaidContent(title: String, body: String, mediaSourceKey: String): UUID {
        val createResponse =
            restTemplate.postForEntity(
                "/advanced/contents/paid",
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
        return UUID.fromString(json(createResponse.body).required("contentId").asText())
    }

    private fun submitContentForReview(contentId: UUID) {
        val submitResponse =
            restTemplate.postForEntity(
                "/contents/$contentId/submit-review",
                HttpEntity.EMPTY,
                String::class.java,
            )
        assertThat(submitResponse.statusCode).isEqualTo(HttpStatus.OK)
    }

    private fun approveContent(contentId: UUID) {
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

    private fun waitForSubmittedMediaExternalTaskId(contentId: UUID): String {
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
    }

    private fun assertPublishedContent(contentId: UUID) {
        val content = waitForJson(Duration.ofSeconds(10)) {
            val response = restTemplate.getForEntity("/contents/$contentId", String::class.java)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            json(response.body).takeIf { root ->
                root.path("contentStatus").asText() == "PUBLISHED"
            }
        }
        assertThat(content.required("contentStatus").asText()).isEqualTo("PUBLISHED")
    }

    private fun assertContentState(contentId: UUID, reviewStatus: String, contentStatus: String) {
        val response = restTemplate.getForEntity("/contents/$contentId", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val content = json(response.body)
        assertThat(content.required("reviewStatus").asText()).isEqualTo(reviewStatus)
        assertThat(content.required("contentStatus").asText()).isEqualTo(contentStatus)
    }

    private fun assertNoPaidPublicationTask(timeout: Duration, contentId: UUID) {
        val deadlineNanos = System.nanoTime() + timeout.toNanos()
        var latest: PaidPublicationTaskRow? = null
        while (System.nanoTime() < deadlineNanos) {
            latest = paidPublicationTask(contentId)
            assertThat(latest)
                .describedAs("paid publication task must not start while review is pending")
                .isNull()
            Thread.sleep(100)
        }
    }

    private fun sagaProcessCodes(contentId: UUID): List<String> =
        jdbcTemplate.queryForList(
            """
            select process.process_code
            from __saga_process process
            join __saga saga on saga.id = process.saga_id
            join paid_publication_task task on task.publication_saga_id = saga.saga_uuid
            where task.content_id = ?
            order by process.id
            """.trimIndent(),
            String::class.java,
            contentId,
        )

    private fun waitForPaidPublicationTask(
        timeout: Duration,
        contentId: UUID,
        ready: (PaidPublicationTaskRow) -> Boolean,
    ): PaidPublicationTaskRow {
        val deadlineNanos = System.nanoTime() + timeout.toNanos()
        var latest: PaidPublicationTaskRow? = null
        while (System.nanoTime() < deadlineNanos) {
            latest = paidPublicationTask(contentId)
            if (latest != null && ready(latest)) {
                return latest
            }
            Thread.sleep(100)
        }
        return checkNotNull(latest) {
            "Timed out after $timeout while waiting for paid publication task."
        }.also {
            assertThat(ready(it))
                .describedAs("last paid publication task row was $it")
                .isTrue()
        }
    }

    private fun paidPublicationTask(contentId: UUID): PaidPublicationTaskRow? =
        jdbcTemplate.query(
            """
            select paid_publication_status, publication_saga_id, payout_hold_status, payout_hold_id, entitlement_plan_status
            from paid_publication_task
            where content_id = ?
            """.trimIndent(),
            { rs, _ ->
                PaidPublicationTaskRow(
                    paidPublicationStatus = rs.getInt("paid_publication_status"),
                    publicationSagaId = rs.getString("publication_saga_id"),
                    payoutHoldStatus = rs.getInt("payout_hold_status"),
                    payoutHoldId = rs.getString("payout_hold_id"),
                    entitlementPlanStatus = rs.getInt("entitlement_plan_status"),
                )
            },
            contentId,
        ).firstOrNull()

    private fun contentStatus(contentId: UUID): Int =
        jdbcTemplate.queryForObject(
            """
            select content_status
            from content
            where id = ?
            """.trimIndent(),
            Int::class.java,
            contentId,
        )!!

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

    data class PaidPublicationTaskRow(
        val paidPublicationStatus: Int,
        val publicationSagaId: String?,
        val payoutHoldStatus: Int,
        val payoutHoldId: String?,
        val entitlementPlanStatus: Int,
    )
}
