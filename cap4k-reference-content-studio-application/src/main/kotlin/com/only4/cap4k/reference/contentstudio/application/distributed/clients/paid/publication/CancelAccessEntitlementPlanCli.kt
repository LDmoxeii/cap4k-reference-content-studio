package com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication

import com.only4.cap4k.ddd.core.application.RequestParam
import java.util.UUID

object CancelAccessEntitlementPlanCli {

    data class Request(
        val paidPublicationTaskId: UUID
    ) : RequestParam<Response>

    data class Response(
        val cancelled: Boolean
    )

}
