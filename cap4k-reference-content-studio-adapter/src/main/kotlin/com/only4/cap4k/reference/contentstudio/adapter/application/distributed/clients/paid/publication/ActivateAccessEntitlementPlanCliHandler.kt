package com.only4.cap4k.reference.contentstudio.adapter.application.distributed.clients.paid.publication

import com.only4.cap4k.ddd.core.application.RequestHandler
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.ActivateAccessEntitlementPlanCli
import org.springframework.stereotype.Service

@Service
class ActivateAccessEntitlementPlanCliHandler(
    private val state: FakePaidPublicationCliState
) : RequestHandler<ActivateAccessEntitlementPlanCli.Request, ActivateAccessEntitlementPlanCli.Response> {

    override fun exec(request: ActivateAccessEntitlementPlanCli.Request): ActivateAccessEntitlementPlanCli.Response {
        if (state.shouldFailActivation()) {
            return ActivateAccessEntitlementPlanCli.Response(
                activated = false
            )
        }
        return ActivateAccessEntitlementPlanCli.Response(
            activated = true
        )
    }
}
