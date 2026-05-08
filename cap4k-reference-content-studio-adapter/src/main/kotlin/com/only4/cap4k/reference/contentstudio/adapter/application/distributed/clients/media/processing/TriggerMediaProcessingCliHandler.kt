package com.only4.cap4k.reference.contentstudio.adapter.application.distributed.clients.media.processing

import com.only4.cap4k.ddd.core.application.RequestHandler
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.media.processing.TriggerMediaProcessingCli
import org.springframework.stereotype.Service

@Service
class TriggerMediaProcessingCliHandler : RequestHandler<TriggerMediaProcessingCli.Request, TriggerMediaProcessingCli.Response> {

    override fun exec(request: TriggerMediaProcessingCli.Request): TriggerMediaProcessingCli.Response {
        return TriggerMediaProcessingCli.Response(
            accepted = TODO("set accepted"),
            externalTaskId = TODO("set externalTaskId")
        )
    }
}
