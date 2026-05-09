
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.read

import java.util.UUID

object GetMediaProcessingStatusPayload {

    data class Request(
        val contentId: UUID
    )

    data class Response(
        val contentId: UUID,
        val task: Task?
    ) {
        data class Task(
            val taskId: UUID,
            val externalTaskId: String?,
            val processingStatus: String
        )
    }

}
