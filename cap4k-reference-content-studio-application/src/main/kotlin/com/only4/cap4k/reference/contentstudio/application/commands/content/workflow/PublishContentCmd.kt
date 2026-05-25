package com.only4.cap4k.reference.contentstudio.application.commands.content.workflow

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.isReadyForImmediatePublication
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.publish
import com.only4.cap4k.reference.contentstudio.domain._share.meta.content.SContent
import java.time.LocalDateTime
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import org.springframework.stereotype.Service

object PublishContentCmd {

    @Service
    open class Handler : Command<Request, Response> {

        open override fun exec(request: Request): Response {
            val content = checkNotNull(Mediator.repositories.findOne(SContent.predicateById(request.contentId), persist = true)) {
                "Content ${request.contentId} was not found."
            }
            when (val decision = decideLoadedContent(content)) {
                Decision.NotImmediateContent,
                Decision.NotPublicationReady -> return Response(published = false, decision = decision)

                Decision.Publishable -> Unit
            }

            content.publish(request.publishedAt)
            Mediator.uow.save()

            return Response(published = true, decision = Decision.Publishable)
        }
    }

    fun decideLoadedContent(content: Content): Decision =
        when {
            content.releasePolicy != ReleasePolicy.IMMEDIATE -> Decision.NotImmediateContent
            !content.isReadyForImmediatePublication() -> Decision.NotPublicationReady
            else -> Decision.Publishable
        }

    enum class Decision {
        Publishable,
        NotImmediateContent,
        NotPublicationReady,
    }

    data class Request(
        val contentId: ContentId,
        val publishedAt: LocalDateTime
    ) : RequestParam<Response>

    data class Response(
        val published: Boolean,
        val decision: Decision,
    )

}
