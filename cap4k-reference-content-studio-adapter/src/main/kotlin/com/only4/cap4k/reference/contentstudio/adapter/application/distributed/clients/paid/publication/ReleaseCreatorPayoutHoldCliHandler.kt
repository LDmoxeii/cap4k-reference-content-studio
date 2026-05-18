package com.only4.cap4k.reference.contentstudio.adapter.application.distributed.clients.paid.publication

import com.only4.cap4k.ddd.core.application.RequestHandler
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.ReleaseCreatorPayoutHoldCli
import org.springframework.stereotype.Service

@Service
class ReleaseCreatorPayoutHoldCliHandler : RequestHandler<ReleaseCreatorPayoutHoldCli.Request, ReleaseCreatorPayoutHoldCli.Response> {

    override fun exec(request: ReleaseCreatorPayoutHoldCli.Request): ReleaseCreatorPayoutHoldCli.Response {
        return ReleaseCreatorPayoutHoldCli.Response(
            released = true
        )
    }
}
