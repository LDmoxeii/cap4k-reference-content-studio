package com.only4.cap4k.reference.contentstudio.application.distributed.clients.media.processing

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId

object TriggerMediaProcessingCli {

    data class Request(
        val contentId: ContentId,
        val mediaSourceKey: String
    ) : RequestParam<Response>

    data class Response(
        val accepted: Boolean,
        val externalTaskId: String?
    )

}
