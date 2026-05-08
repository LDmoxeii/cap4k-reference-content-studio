package com.only4.cap4k.reference.contentstudio.adapter.http

import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.CreateContentDraftCmd
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/contents")
class ContentController(
    private val createContentDraftHandler: CreateContentDraftCmd.Handler,
) {

    @PostMapping
    fun create(@RequestBody request: CreateContentDraftRequest): CreateContentDraftCmd.Response =
        createContentDraftHandler.exec(
            CreateContentDraftCmd.Request(
                title = request.title,
                body = request.body,
                mediaSourceKey = request.mediaSourceKey,
            )
        )

    data class CreateContentDraftRequest(
        val title: String,
        val body: String,
        val mediaSourceKey: String,
    )
}
