package com.only4.cap4k.reference.contentstudio.start

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.only4.cap4k.reference.contentstudio.application.subscribers.integration.inbound.media.processing.MediaProcessingCallbackIntegrationEvent
import java.time.Duration
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootTest(
    classes = [ContentStudioApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.datasource.url=jdbc:h2:mem:content-studio-paid-success-test;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "server.port=0",
    ],
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ContentStudioPaidPublicationSagaSuccessSmokeTest(
    @param:Autowired private val restTemplate: TestRestTemplate,
    @param:Autowired private val objectMapper: ObjectMapper,
    @param:Autowired private val jdbcTemplate: JdbcTemplate,
) : ContentStudioPaidPublicationSagaSmokeSupport(restTemplate, objectMapper, jdbcTemplate) {

    @Test
    fun `paid publication saga publishes content and activates entitlement plan`() {
        val contentId = runPaidPublicationPath()

        val task = waitForPaidPublicationTask(Duration.ofSeconds(10), contentId) { row ->
            row.paidPublicationStatus == 2 && row.payoutHoldStatus == 1 && row.entitlementPlanStatus == 2
        }
        assertThat(task.paidPublicationStatus).isEqualTo(2)
        assertThat(task.payoutHoldStatus).isEqualTo(1)
        assertThat(task.entitlementPlanStatus).isEqualTo(2)
        assertThat(sagaProcessCodes())
            .contains(
                "reserve-payout-hold",
                "create-entitlement-plan",
                "publish-content",
                "activate-entitlement-plan",
            )
    }
}

@SpringBootTest(
    classes = [ContentStudioApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.datasource.url=jdbc:h2:mem:content-studio-paid-failure-test;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "server.port=0",
        "contentStudio.fakeEntitlement.failActivation=true",
    ],
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ContentStudioPaidPublicationSagaManualRepairSmokeTest(
    @param:Autowired private val restTemplate: TestRestTemplate,
    @param:Autowired private val objectMapper: ObjectMapper,
    @param:Autowired private val jdbcTemplate: JdbcTemplate,
) : ContentStudioPaidPublicationSagaSmokeSupport(restTemplate, objectMapper, jdbcTemplate) {

    @Test
    fun `paid publication saga marks manual repair when entitlement activation fails after publish`() {
        val contentId = runPaidPublicationPath()

        val task = waitForPaidPublicationTask(Duration.ofSeconds(10), contentId) { row ->
            row.paidPublicationStatus == 4
        }
        assertThat(task.paidPublicationStatus).isEqualTo(4)
        assertThat(task.payoutHoldStatus).isEqualTo(2)
        assertThat(task.entitlementPlanStatus).isEqualTo(3)
    }
}

abstract class ContentStudioPaidPublicationSagaSmokeSupport(
    private val restTemplate: TestRestTemplate,
    private val objectMapper: ObjectMapper,
    private val jdbcTemplate: JdbcTemplate,
) {

    protected fun runPaidPublicationPath(): UUID {
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

    protected fun sagaProcessCodes(): List<String> =
        jdbcTemplate.queryForList(
            "select process_code from __saga_process order by id",
            String::class.java,
        )

    protected fun waitForPaidPublicationTask(
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
