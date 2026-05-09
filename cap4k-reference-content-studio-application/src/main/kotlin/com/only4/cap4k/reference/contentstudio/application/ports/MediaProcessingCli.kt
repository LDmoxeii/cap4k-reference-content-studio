package com.only4.cap4k.reference.contentstudio.application.ports

import java.util.UUID

interface MediaProcessingCli {
    fun start(contentId: UUID, mediaSourceKey: String): Response

    fun getStatus(externalTaskId: String): StatusResponse

    data class Response(
        val accepted: Boolean,
        val externalTaskId: String?,
    )

    data class StatusResponse(
        val status: ExternalTaskStatus,
    )

    enum class ExternalTaskStatus {
        SUBMITTED,
        SUCCEEDED,
    }
}
