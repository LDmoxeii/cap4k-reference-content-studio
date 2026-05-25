package com.only4.cap4k.reference.contentstudio.application.distributed.clients.paid.publication

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId

object CancelAccessEntitlementPlanCli {

    data class Request(
        val paidPublicationTaskId: PaidPublicationTaskId
    ) : RequestParam<Response>

    data class Response(
        val cancelled: Boolean
    )

}
