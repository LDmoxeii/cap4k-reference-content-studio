package com.only4.cap4k.reference.contentstudio.adapter.application.distributed.clients.paid.publication

import com.only4.cap4k.ddd.core.application.RequestHandler
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.ReserveCreatorPayoutHoldCli
import org.springframework.stereotype.Service
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

@Service
@BuildingBlock(
    tag = "client",
    name = "ReserveCreatorPayoutHold",
    packageName = "paid.publication",
    description = "reserve creator payout hold client",
    aggregates = ["PaidPublicationTask"],
    family = "client-handler"
)
class ReserveCreatorPayoutHoldCliHandler : RequestHandler<ReserveCreatorPayoutHoldCli.Request, ReserveCreatorPayoutHoldCli.Response> {

    override fun exec(request: ReserveCreatorPayoutHoldCli.Request): ReserveCreatorPayoutHoldCli.Response {
        return ReserveCreatorPayoutHoldCli.Response(
            payoutHoldId = "hold-${request.paidPublicationTaskId}"
        )
    }
}
