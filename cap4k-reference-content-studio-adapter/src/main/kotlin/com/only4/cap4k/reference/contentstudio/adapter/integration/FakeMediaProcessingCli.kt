package com.only4.cap4k.reference.contentstudio.adapter.integration

import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.springframework.stereotype.Service

@Service
class FakeMediaProcessingCli {
    private val externalTaskIds = ConcurrentHashMap<UUID, String>()
    private val statuses = ConcurrentHashMap<String, String>()

    fun start(
        contentId: UUID,
        mediaSourceKey: String,
    ): com.only4.cap4k.reference.contentstudio.application.distributed.clients.media.processing.TriggerMediaProcessingCli.Response {
        val externalTaskId = externalTaskIds.computeIfAbsent(contentId) {
            "fake-media-$contentId"
        }
        statuses.putIfAbsent(externalTaskId, "SUBMITTED")
        return com.only4.cap4k.reference.contentstudio.application.distributed.clients.media.processing.TriggerMediaProcessingCli.Response(
            accepted = true,
            externalTaskId = externalTaskId,
        )
    }

    fun getStatus(externalTaskId: String): String = statuses[externalTaskId] ?: "SUBMITTED"

    fun getSucceededAssetSha256(): String = "a".repeat(64)

    fun getSucceededAssetLocation(externalTaskId: String): String = "s3://content-studio/assets/$externalTaskId.mp4"

    fun getSucceededCompletedAt(): LocalDateTime = LocalDateTime.parse("2026-05-11T10:15:30")
}
