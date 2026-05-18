package com.only4.cap4k.reference.contentstudio.adapter.http

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.workflow.CreatePaidContentDraftPayload
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.CreatePaidContentDraftCmd
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/advanced/contents")
class AdvancedPaidPublicationController {

    @PostMapping("/paid")
    fun createPaid(@RequestBody request: CreatePaidContentDraftPayload.Request): CreatePaidContentDraftPayload.Response {
        val response =
            Mediator.cmd.send(
                CreatePaidContentDraftCmd.Request(
                    title = request.title,
                    body = request.body,
                    mediaSourceKey = request.mediaSourceKey,
                )
            )

        return CreatePaidContentDraftPayload.Response(contentId = response.contentId)
    }
}
