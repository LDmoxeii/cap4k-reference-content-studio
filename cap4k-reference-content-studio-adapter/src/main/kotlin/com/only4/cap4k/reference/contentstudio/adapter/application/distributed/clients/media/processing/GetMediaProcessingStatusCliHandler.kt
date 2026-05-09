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
        return GetMediaProcessingStatusCli.Response(
            status = fakeMediaProcessingCli.getStatus(request.externalTaskId)
        )
    }
}
