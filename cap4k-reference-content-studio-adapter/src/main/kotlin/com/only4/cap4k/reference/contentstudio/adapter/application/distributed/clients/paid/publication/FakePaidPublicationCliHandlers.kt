package com.only4.cap4k.reference.contentstudio.adapter.application.distributed.clients.paid.publication

import com.only4.cap4k.ddd.core.application.RequestHandler
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.ActivateAccessEntitlementPlanCli
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.CancelAccessEntitlementPlanCli
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.CreateAccessEntitlementPlanCli
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.ReleaseCreatorPayoutHoldCli
import com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication.ReserveCreatorPayoutHoldCli
import java.util.concurrent.atomic.AtomicBoolean
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class FakePaidPublicationCliState(
    @Value("\${contentStudio.fakeEntitlement.failActivation:false}") initialFailActivation: Boolean
) {
    private val failActivation = AtomicBoolean(initialFailActivation)

    fun shouldFailActivation(): Boolean = failActivation.get()

    fun setFailActivation(value: Boolean) {
        failActivation.set(value)
    }
}

@Service
class ReserveCreatorPayoutHoldCliHandler : RequestHandler<ReserveCreatorPayoutHoldCli.Request, ReserveCreatorPayoutHoldCli.Response> {

    override fun exec(request: ReserveCreatorPayoutHoldCli.Request): ReserveCreatorPayoutHoldCli.Response {
        return ReserveCreatorPayoutHoldCli.Response(
            payoutHoldId = "hold-${request.paidPublicationTaskId}"
        )
    }
}

@Service
class ReleaseCreatorPayoutHoldCliHandler : RequestHandler<ReleaseCreatorPayoutHoldCli.Request, ReleaseCreatorPayoutHoldCli.Response> {

    override fun exec(request: ReleaseCreatorPayoutHoldCli.Request): ReleaseCreatorPayoutHoldCli.Response {
        return ReleaseCreatorPayoutHoldCli.Response(
            released = true
        )
    }
}

@Service
class CreateAccessEntitlementPlanCliHandler : RequestHandler<CreateAccessEntitlementPlanCli.Request, CreateAccessEntitlementPlanCli.Response> {

    override fun exec(request: CreateAccessEntitlementPlanCli.Request): CreateAccessEntitlementPlanCli.Response {
        return CreateAccessEntitlementPlanCli.Response(
            entitlementPlanId = "plan-${request.paidPublicationTaskId}"
        )
    }
}

@Service
class CancelAccessEntitlementPlanCliHandler : RequestHandler<CancelAccessEntitlementPlanCli.Request, CancelAccessEntitlementPlanCli.Response> {

    override fun exec(request: CancelAccessEntitlementPlanCli.Request): CancelAccessEntitlementPlanCli.Response {
        return CancelAccessEntitlementPlanCli.Response(
            cancelled = true
        )
    }
}

@Service
class ActivateAccessEntitlementPlanCliHandler(
    private val state: FakePaidPublicationCliState
) : RequestHandler<ActivateAccessEntitlementPlanCli.Request, ActivateAccessEntitlementPlanCli.Response> {

    override fun exec(request: ActivateAccessEntitlementPlanCli.Request): ActivateAccessEntitlementPlanCli.Response {
        if (state.shouldFailActivation()) {
            throw IllegalStateException("Fake entitlement activation failed.")
        }
        return ActivateAccessEntitlementPlanCli.Response(
            activated = true
        )
    }
}
