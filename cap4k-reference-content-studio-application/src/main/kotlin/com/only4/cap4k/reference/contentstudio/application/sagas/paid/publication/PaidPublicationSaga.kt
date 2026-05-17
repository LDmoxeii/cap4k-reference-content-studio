package com.only4.cap4k.reference.contentstudio.application.sagas.paid.publication

import com.only4.cap4k.ddd.core.application.saga.SagaParam
import java.util.UUID

object PaidPublicationSaga {

    data class Request(
        val paidPublicationTaskId: UUID
    ) : SagaParam<Response>

    data class Response(
        val completed: Boolean = true
    )
}
