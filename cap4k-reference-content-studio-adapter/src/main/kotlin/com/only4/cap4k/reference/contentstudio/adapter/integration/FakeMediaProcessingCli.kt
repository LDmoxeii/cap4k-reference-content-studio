package com.only4.cap4k.reference.contentstudio.adapter.integration

import java.time.LocalDateTime
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import java.util.concurrent.ConcurrentHashMap
import org.springframework.stereotype.Service

@Service
class FakeMediaProcessingCli {
    private val externalTaskIds = ConcurrentHashMap<ContentId, String>()
    private val statuses = ConcurrentHashMap<String, String>()

    fun start(
        contentId: ContentId,
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
