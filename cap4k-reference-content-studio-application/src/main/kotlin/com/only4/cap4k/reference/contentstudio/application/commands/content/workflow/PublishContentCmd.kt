package com.only4.cap4k.reference.contentstudio.application.commands.content.workflow

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.publish
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain._share.meta.content.SContent
import com.only4.cap4k.reference.contentstudio.domain._share.meta.media_processing_task.SMediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.services.PublicationEligibilityDecision
import com.only4.cap4k.reference.contentstudio.domain.services.PublicationEligibilityDomainService
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object PublishContentCmd {

    @Service
    open class Handler : Command<Request, Response> {

        open override fun exec(request: Request): Response {
            val content = checkNotNull(Mediator.repositories.findOne(SContent.predicateById(request.contentId))) {
                "Content ${request.contentId} was not found."
            }
            val mediaProcessingTask =
                Mediator.repositories.findFirst(
                    SMediaProcessingTask.predicate { schema ->
                        schema.contentId.eq(request.contentId)
                    }
                ) ?: return Response(published = false)
            val publicationEligibilityDomainService =
                Mediator.services.getService(PublicationEligibilityDomainService::class.java)
            val policyGateSatisfied =
                content.releasePolicy == ReleasePolicy.IMMEDIATE || request.policyGateSatisfied
            val decision =
                publicationEligibilityDomainService.evaluate(
                    content = content,
                    task = mediaProcessingTask,
                    policyGateSatisfied = policyGateSatisfied,
                )
            when (decision) {
                PublicationEligibilityDecision.Eligible -> Unit
                PublicationEligibilityDecision.ContentNotApproved,
                PublicationEligibilityDecision.MediaProcessingNotSucceeded,
                PublicationEligibilityDecision.PolicyGateNotSatisfied -> return Response(published = false)
                PublicationEligibilityDecision.TaskDoesNotBelongToContent ->
                    error("Content ${request.contentId} is not eligible for publication: $decision.")
            }
            content.publish(request.publishedAt)
            Mediator.uow.save()

            return Response()
        }
    }

    data class Request(
        val contentId: UUID,
        val publishedAt: LocalDateTime,
        val policyGateSatisfied: Boolean = false
    ) : RequestParam<Response>

    data class Response(
        val published: Boolean = true
    )

}
