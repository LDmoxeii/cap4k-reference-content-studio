package com.only4.cap4k.reference.contentstudio.adapter.integration

import com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingCli
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.springframework.stereotype.Service

@Service
class FakeMediaProcessingCli : MediaProcessingCli {
    private val externalTaskIds = ConcurrentHashMap<UUID, String>()
    private val statuses = ConcurrentHashMap<String, MediaProcessingCli.ExternalTaskStatus>()

    override fun start(contentId: UUID, mediaSourceKey: String): MediaProcessingCli.Response {
        val externalTaskId = externalTaskIds.computeIfAbsent(contentId) {
            "fake-media-$contentId"
        }
        statuses.putIfAbsent(externalTaskId, MediaProcessingCli.ExternalTaskStatus.SUBMITTED)
        return MediaProcessingCli.Response(
            accepted = true,
            externalTaskId = externalTaskId,
        )
    }

    override fun getStatus(externalTaskId: String): MediaProcessingCli.StatusResponse =
        MediaProcessingCli.StatusResponse(
            status = statuses[externalTaskId] ?: MediaProcessingCli.ExternalTaskStatus.SUBMITTED,
        )
}
