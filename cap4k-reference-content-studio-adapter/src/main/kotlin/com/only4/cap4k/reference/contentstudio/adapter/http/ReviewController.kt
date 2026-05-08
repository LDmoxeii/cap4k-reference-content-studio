package com.only4.cap4k.reference.contentstudio.adapter.http

import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.ApproveContentReviewCmd
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.SubmitContentForReviewCmd
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/contents/{contentId}")
class ReviewController(
    private val submitContentForReviewHandler: SubmitContentForReviewCmd.Handler,
    private val approveContentReviewHandler: ApproveContentReviewCmd.Handler,
) {

    @PostMapping("/submit-review")
    fun submitReview(@PathVariable contentId: UUID): SubmitContentForReviewCmd.Response =
        submitContentForReviewHandler.exec(SubmitContentForReviewCmd.Request(contentId = contentId))

    @PostMapping("/approve")
    fun approve(
        @PathVariable contentId: UUID,
        @RequestBody request: ApproveContentReviewRequest,
    ): ApproveContentReviewCmd.Response =
        approveContentReviewHandler.exec(
            ApproveContentReviewCmd.Request(
                contentId = contentId,
                reviewerId = request.reviewerId,
                reviewedAt = LocalDateTime.now(),
            )
        )

    data class ApproveContentReviewRequest(
        val reviewerId: UUID,
    )
}
