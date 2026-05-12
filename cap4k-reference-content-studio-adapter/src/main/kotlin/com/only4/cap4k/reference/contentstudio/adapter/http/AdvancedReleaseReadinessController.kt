package com.only4.cap4k.reference.contentstudio.adapter.http

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.workflow.CreateGatedContentDraftPayload
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.CreateGatedContentDraftCmd
import com.only4.cap4k.reference.contentstudio.application.commands.release.readiness.ConfirmManualReleaseCmd
import com.only4.cap4k.reference.contentstudio.application.commands.release.readiness.PassCopyrightReviewCmd
import java.util.UUID
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/advanced/contents")
class AdvancedReleaseReadinessController {

    @PostMapping("/gated")
    fun createGated(@RequestBody request: CreateGatedContentDraftPayload.Request): CreateGatedContentDraftPayload.Response {
        val response =
            Mediator.cmd.send(
                CreateGatedContentDraftCmd.Request(
                    title = request.title,
                    body = request.body,
                    mediaSourceKey = request.mediaSourceKey,
                    releaseWindowOpensAt = request.releaseWindowOpensAt,
                    releaseWindowClosesAt = request.releaseWindowClosesAt,
                )
            )

        return CreateGatedContentDraftPayload.Response(contentId = response.contentId)
    }

    @PostMapping("/{contentId}/release-readiness/copyright-pass")
    fun passCopyright(@PathVariable contentId: UUID) {
        Mediator.cmd.send(PassCopyrightReviewCmd.Request(contentId = contentId))
    }

    @PostMapping("/{contentId}/release-readiness/manual-confirm")
    fun confirmManual(@PathVariable contentId: UUID) {
        Mediator.cmd.send(ConfirmManualReleaseCmd.Request(contentId = contentId))
    }
}
