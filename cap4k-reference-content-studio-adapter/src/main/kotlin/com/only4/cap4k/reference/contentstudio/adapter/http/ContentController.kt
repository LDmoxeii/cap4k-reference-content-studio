package com.only4.cap4k.reference.contentstudio.adapter.http

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.workflow.CreateContentDraftPayload
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.CreateContentDraftCmd
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/contents")
class ContentController {

    @PostMapping
    fun create(@RequestBody request: CreateContentDraftPayload.Request): CreateContentDraftPayload.Response {
        val response =
            Mediator.cmd.send(
                CreateContentDraftCmd.Request(
                    title = request.title,
                    body = request.body,
                    mediaSourceKey = request.mediaSourceKey,
                )
            )

        return CreateContentDraftPayload.Response(contentId = response.contentId)
    }
}
