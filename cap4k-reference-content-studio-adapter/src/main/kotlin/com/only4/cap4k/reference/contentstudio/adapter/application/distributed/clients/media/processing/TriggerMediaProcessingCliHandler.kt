package com.only4.cap4k.reference.contentstudio.adapter.application.distributed.clients.media.processing

import com.only4.cap4k.ddd.core.application.RequestHandler
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.media.processing.TriggerMediaProcessingCli
import com.only4.cap4k.reference.contentstudio.adapter.integration.FakeMediaProcessingCli
import org.springframework.stereotype.Service
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

@Service
@BuildingBlock(
    tag = "client",
    name = "TriggerMediaProcessing",
    packageName = "media.processing",
    description = "trigger media processing cli task",
    aggregates = ["Content"],
    family = "client-handler"
)
class TriggerMediaProcessingCliHandler(
    private val fakeMediaProcessingCli: FakeMediaProcessingCli,
) : RequestHandler<TriggerMediaProcessingCli.Request, TriggerMediaProcessingCli.Response> {

    override fun exec(request: TriggerMediaProcessingCli.Request): TriggerMediaProcessingCli.Response {
        return fakeMediaProcessingCli.start(request.contentId, request.mediaSourceKey)
    }
}
