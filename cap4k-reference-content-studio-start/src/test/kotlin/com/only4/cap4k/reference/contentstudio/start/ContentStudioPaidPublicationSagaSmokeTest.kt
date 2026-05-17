package com.only4.cap4k.reference.contentstudio.start

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.only4.cap4k.reference.contentstudio.adapter.application.distributed.clients.paid.publication.FakePaidPublicationCliState
import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.inbound.media.processing.MediaProcessingCallbackIntegrationEvent
import java.time.Duration
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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

    @BeforeEach
    fun resetPaidPublicationSmokeState() {
        fakePaidPublicationCliState.setFailActivation(false)
        listOf(
            "__archived_saga_process",
            "__saga_process",
            "__archived_saga",
            "__saga",
        ).forEach { table ->
            jdbcTemplate.execute("delete from $table")
        }
    }

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

        val contentId = runPaidPublicationPath()

        val task = waitForPaidPublicationTask(Duration.ofSeconds(10), contentId) { row ->
            row.paidPublicationStatus == 4
        }
        assertThat(task.paidPublicationStatus).isEqualTo(4)
        assertThat(task.payoutHoldStatus).isEqualTo(2)
        assertThat(task.entitlementPlanStatus).isEqualTo(3)
    }

    private fun runPaidPublicationPath(): UUID {
        val createResponse =
            restTemplate.postForEntity(
                "/advanced/contents/paid",
                jsonRequest(
                    """
                    {
                      "title": "Paid HTTP path",
                      "body": "Exercise the paid publication saga path",
                      "mediaSourceKey": "media/paid-http-smoke.mp4"
                    }
                    """.trimIndent()
                ),
                String::class.java,
            )

        assertThat(createResponse.statusCode).isEqualTo(HttpStatus.OK)
        val contentId = UUID.fromString(json(createResponse.body).required("contentId").asText())

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

        val content = waitForJson(Duration.ofSeconds(10)) {
            val response = restTemplate.getForEntity("/contents/$contentId", String::class.java)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            json(response.body).takeIf { root ->
                root.path("contentStatus").asText() == "PUBLISHED"
            }
        }
        assertThat(content.required("contentStatus").asText()).isEqualTo("PUBLISHED")

        return contentId
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
            select paid_publication_status, payout_hold_status, entitlement_plan_status
            from paid_publication_task
            where content_id = ?
            """.trimIndent(),
            { rs, _ ->
                PaidPublicationTaskRow(
                    paidPublicationStatus = rs.getInt("paid_publication_status"),
                    payoutHoldStatus = rs.getInt("payout_hold_status"),
                    entitlementPlanStatus = rs.getInt("entitlement_plan_status"),
                )
            },
            contentId,
        ).firstOrNull()

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
        val payoutHoldStatus: Int,
        val entitlementPlanStatus: Int,
    )
}
