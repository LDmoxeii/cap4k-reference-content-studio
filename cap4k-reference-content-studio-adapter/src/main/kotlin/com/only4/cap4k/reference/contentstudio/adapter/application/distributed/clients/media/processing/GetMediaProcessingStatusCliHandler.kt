package com.only4.cap4k.reference.contentstudio.adapter.application.distributed.clients.media.processing

import com.only4.cap4k.ddd.core.application.RequestHandler
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.media.processing.GetMediaProcessingStatusCli
import com.only4.cap4k.reference.contentstudio.adapter.integration.FakeMediaProcessingCli
import org.springframework.stereotype.Service

@Service
class GetMediaProcessingStatusCliHandler(
    private val fakeMediaProcessingCli: FakeMediaProcessingCli,
) : RequestHandler<GetMediaProcessingStatusCli.Request, GetMediaProcessingStatusCli.Response> {

    override fun exec(request: GetMediaProcessingStatusCli.Request): GetMediaProcessingStatusCli.Response {
        val status = fakeMediaProcessingCli.getStatus(request.externalTaskId)
        return GetMediaProcessingStatusCli.Response(
            status = status,
            assetSha256 = if (status == "SUCCEEDED") {
                fakeMediaProcessingCli.getSucceededAssetSha256()
            } else {
                null
            },
            assetLocation = if (status == "SUCCEEDED") {
                fakeMediaProcessingCli.getSucceededAssetLocation(request.externalTaskId)
            } else {
                null
            },
            completedAt = if (status == "SUCCEEDED") {
                fakeMediaProcessingCli.getSucceededCompletedAt()
            } else {
                null
            },
        )
    }
}
