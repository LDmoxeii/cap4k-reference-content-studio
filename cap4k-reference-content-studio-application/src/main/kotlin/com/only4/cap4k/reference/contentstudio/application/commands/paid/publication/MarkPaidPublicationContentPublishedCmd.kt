package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.domain._share.meta.content.SContent
import com.only4.cap4k.reference.contentstudio.domain._share.meta.paid_publication_task.SPaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.markPublished
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTaskId
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTaskId
import com.only4.cap4k.reference.contentstudio.domain.shared.ids.ReviewerId
import org.springframework.stereotype.Service

object MarkPaidPublicationContentPublishedCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val task = loadTask(request.paidPublicationTaskId)
            if (task.paidPublicationStatus == PaidPublicationStatus.PUBLISHED) {
                return Response(marked = false)
            }
            check(task.paidPublicationStatus == PaidPublicationStatus.RUNNING) {
                "Paid publication task ${task.id} is not running."
            }
            check(task.payoutHoldStatus == PayoutHoldStatus.RESERVED) {
                "Paid publication task ${task.id} has no reserved payout hold."
            }
            check(task.entitlementPlanStatus == EntitlementPlanStatus.CREATED) {
                "Paid publication task ${task.id} has no created entitlement plan."
            }

            val content = loadContent(task.contentId)
            check(content.releasePolicy == ReleasePolicy.PAID) {
                "Paid publication task ${task.id} requires paid content, but content ${content.id} has release policy ${content.releasePolicy}."
            }
            if (content.contentStatus != ContentStatus.PUBLISHED) {
                return Response(marked = false)
            }

            task.markPublished(requireNotNull(content.publishedAt) {
                "Content ${content.id} must have publishedAt when content status is PUBLISHED."
            })
            Mediator.uow.save()

            return Response(
                marked = true
            )
        }
    }

    data class Request(
        val paidPublicationTaskId: PaidPublicationTaskId
    ) : RequestParam<Response>

    data class Response(
        val marked: Boolean
    )

    private fun loadTask(paidPublicationTaskId: PaidPublicationTaskId): PaidPublicationTask =
        checkNotNull(Mediator.repositories.findOne(SPaidPublicationTask.predicateById(paidPublicationTaskId), persist = true)) {
            "Paid publication task $paidPublicationTaskId was not found."
        }

    private fun loadContent(contentId: ContentId): Content =
        checkNotNull(Mediator.repositories.findOne(SContent.predicateById(contentId))) {
            "Content $contentId was not found."
        }
}
