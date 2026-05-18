package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.domain._share.meta.content.SContent
import com.only4.cap4k.reference.contentstudio.domain._share.meta.media_processing_task.SMediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain._share.meta.paid_publication_task.SPaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.publish
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.markPublished
import com.only4.cap4k.reference.contentstudio.domain.services.PublicationEligibilityDecision
import com.only4.cap4k.reference.contentstudio.domain.services.PublicationEligibilityDomainService
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object PublishPaidPublicationContentCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val task = loadTask(request.paidPublicationTaskId)
            if (task.paidPublicationStatus == PaidPublicationStatus.PUBLISHED) {
                return Response(published = false)
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

            val now = LocalDateTime.now()
            val content = loadContent(task.contentId)
            val mediaProcessingTask = loadMediaProcessingTask(task.contentId) ?: return Response(published = false)
            val publicationEligibilityDomainService =
                Mediator.services.getService(PublicationEligibilityDomainService::class.java)
            val decision =
                publicationEligibilityDomainService.evaluate(
                    content = content,
                    task = mediaProcessingTask,
                    policyGateSatisfied = true,
                )
            when (decision) {
                PublicationEligibilityDecision.Eligible -> Unit
                PublicationEligibilityDecision.ContentNotApproved,
                PublicationEligibilityDecision.MediaProcessingNotSucceeded,
                PublicationEligibilityDecision.PolicyGateNotSatisfied -> return Response(published = false)
                PublicationEligibilityDecision.TaskDoesNotBelongToContent ->
                    error("Paid publication task ${task.id} is not eligible for publication: $decision.")
            }
            content.publish(now)
            task.markPublished(now)
            Mediator.uow.save()

            return Response(published = true)
        }
    }

    data class Request(
        val paidPublicationTaskId: UUID
    ) : RequestParam<Response>

    data class Response(
        val published: Boolean
    )

    private fun loadTask(paidPublicationTaskId: UUID): PaidPublicationTask =
        checkNotNull(Mediator.repositories.findOne(SPaidPublicationTask.predicateById(paidPublicationTaskId))) {
            "Paid publication task $paidPublicationTaskId was not found."
        }

    private fun loadContent(contentId: UUID): Content =
        checkNotNull(Mediator.repositories.findOne(SContent.predicateById(contentId))) {
            "Content $contentId was not found."
        }

    private fun loadMediaProcessingTask(contentId: UUID): MediaProcessingTask? =
        Mediator.repositories.findFirst(
            SMediaProcessingTask.predicate { schema ->
                schema.contentId.eq(contentId)
            }
        )
}
