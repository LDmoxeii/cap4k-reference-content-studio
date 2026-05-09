
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.workflow

import java.util.UUID

object ApproveContentReviewPayload {

    data class Request(
        val reviewerId: UUID
    )

    class Response

}
