package com.only4.cap4k.reference.contentstudio.adapter.application.distributed.clients.paid.publication

import com.only4.cap4k.ddd.core.application.RequestHandler
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.CancelAccessEntitlementPlanCli
import org.springframework.stereotype.Service
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

@Service
@BuildingBlock(
    tag = "client",
    name = "CancelAccessEntitlementPlan",
    packageName = "paid.publication",
    description = "cancel access entitlement plan client",
    aggregates = ["PaidPublicationTask"],
    family = "client-handler"
)
class CancelAccessEntitlementPlanCliHandler : RequestHandler<CancelAccessEntitlementPlanCli.Request, CancelAccessEntitlementPlanCli.Response> {

    override fun exec(request: CancelAccessEntitlementPlanCli.Request): CancelAccessEntitlementPlanCli.Response {
        return CancelAccessEntitlementPlanCli.Response(
            cancelled = true
        )
    }
}
