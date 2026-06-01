package com.only4.cap4k.reference.contentstudio.adapter.application.distributed.clients.paid.publication

import com.only4.cap4k.ddd.core.application.RequestHandler
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.ReleaseCreatorPayoutHoldCli
import org.springframework.stereotype.Service
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

@Service
@BuildingBlock(
    tag = "client",
    name = "ReleaseCreatorPayoutHold",
    packageName = "paid.publication",
    description = "release creator payout hold client",
    aggregates = ["PaidPublicationTask"],
    family = "client-handler"
)
class ReleaseCreatorPayoutHoldCliHandler : RequestHandler<ReleaseCreatorPayoutHoldCli.Request, ReleaseCreatorPayoutHoldCli.Response> {

    override fun exec(request: ReleaseCreatorPayoutHoldCli.Request): ReleaseCreatorPayoutHoldCli.Response {
        return ReleaseCreatorPayoutHoldCli.Response(
            released = true
        )
    }
}
