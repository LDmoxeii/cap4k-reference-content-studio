package com.only4.cap4k.reference.contentstudio.application.distributed.clients.media.processing

import com.only4.cap4k.ddd.core.application.RequestParam
import java.util.UUID

object TriggerMediaProcessingCli {

    data class Request(
        val contentId: UUID,
        val mediaSourceKey: String
    ) : RequestParam<Response>

    data class Response(
        val accepted: Boolean,
        val externalTaskId: String?
    )

}
