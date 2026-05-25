package com.only4.cap4k.reference.contentstudio.adapter.http

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.workflow.ApproveContentReviewPayload
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.ApproveContentReviewCmd
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.SubmitContentForReviewCmd
import java.time.LocalDateTime
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/contents/{contentId}")
class ReviewController {

    @PostMapping("/submit-review")
    fun submitReview(@PathVariable contentId: ContentId) {
        Mediator.cmd.send(SubmitContentForReviewCmd.Request(contentId = contentId))
    }

    @PostMapping("/approve")
    fun approve(
        @PathVariable contentId: ContentId,
        @RequestBody request: ApproveContentReviewPayload.Request,
    ) {
        Mediator.cmd.send(
            ApproveContentReviewCmd.Request(
                contentId = contentId,
                reviewerId = request.reviewerId,
                reviewedAt = LocalDateTime.now(),
            )
        )
    }
}
