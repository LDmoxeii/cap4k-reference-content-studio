package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.services.paid.publication.PaidPublicationEligibilityService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TryStartPaidPublicationCmdTest {

    @Test
    fun `response exposes non paid content retreat decision`() {
        val response = TryStartPaidPublicationCmd.responseForDecision(
            taskId = null,
            decision = PaidPublicationEligibilityService.Decision.NotPaidContent,
        )

        assertFalse(response.started)
        assertNull(response.taskId)
        assertEquals(PaidPublicationEligibilityService.Decision.NotPaidContent, response.decision)
    }

    @Test
    fun `response keeps existing task id when already started`() {
        val taskId = PaidPublicationTaskId.new()

        val response = TryStartPaidPublicationCmd.responseForDecision(
            taskId = taskId,
            decision = PaidPublicationEligibilityService.Decision.AlreadyStarted,
        )

        assertFalse(response.started)
        assertEquals(taskId, response.taskId)
        assertEquals(PaidPublicationEligibilityService.Decision.AlreadyStarted, response.decision)
    }

    @Test
    fun `started response exposes eligible decision`() {
        val taskId = PaidPublicationTaskId.parse(ContentId.new().toString())

        val response = TryStartPaidPublicationCmd.startedResponse(taskId)

        assertEquals(taskId, response.taskId)
        assertEquals(PaidPublicationEligibilityService.Decision.Eligible, response.decision)
    }
}
