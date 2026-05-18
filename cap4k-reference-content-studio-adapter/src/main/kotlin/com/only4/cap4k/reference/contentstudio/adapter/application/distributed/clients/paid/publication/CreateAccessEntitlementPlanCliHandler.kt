package com.only4.cap4k.reference.contentstudio.adapter.application.distributed.clients.paid.publication

import com.only4.cap4k.ddd.core.application.RequestHandler
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.CreateAccessEntitlementPlanCli
import org.springframework.stereotype.Service

@Service
class CreateAccessEntitlementPlanCliHandler : RequestHandler<CreateAccessEntitlementPlanCli.Request, CreateAccessEntitlementPlanCli.Response> {

    override fun exec(request: CreateAccessEntitlementPlanCli.Request): CreateAccessEntitlementPlanCli.Response {
        return CreateAccessEntitlementPlanCli.Response(
            entitlementPlanId = "plan-${request.paidPublicationTaskId}"
        )
    }
}
