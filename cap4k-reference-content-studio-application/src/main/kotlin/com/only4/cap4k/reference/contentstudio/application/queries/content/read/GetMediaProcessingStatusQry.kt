
package com.only4.cap4k.reference.contentstudio.application.queries.content.read

import com.only4.cap4k.ddd.core.application.RequestParam
import java.util.UUID

object GetMediaProcessingStatusQry {

    data class Request(
        val contentId: UUID
    ) : RequestParam<Response>

    data class Response(
        val contentId: UUID,
        val task: TaskSnapshot?
    ) {
        data class TaskSnapshot(
            val taskId: UUID,
            val externalTaskId: String?,
            val processingStatus: String
        )
    }

}
