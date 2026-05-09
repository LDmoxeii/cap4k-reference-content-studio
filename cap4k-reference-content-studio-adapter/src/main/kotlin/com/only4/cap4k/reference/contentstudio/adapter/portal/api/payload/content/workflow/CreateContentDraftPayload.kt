
package com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.workflow

import java.util.UUID

object CreateContentDraftPayload {

    data class Request(
        val title: String,
        val body: String,
        val mediaSourceKey: String
    )

    data class Response(
        val contentId: UUID
    )

}
