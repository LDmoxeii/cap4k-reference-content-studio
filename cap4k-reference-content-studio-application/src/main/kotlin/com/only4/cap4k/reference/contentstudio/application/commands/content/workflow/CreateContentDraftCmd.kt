package com.only4.cap4k.reference.contentstudio.application.commands.content.workflow

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.recordDraftCreated
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.factory.ContentFactory
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object CreateContentDraftCmd {

    @Service
    open class Handler : Command<Request, Response> {

        open override fun exec(request: Request): Response {
            val now = LocalDateTime.now()
            val content =
                Mediator.factories.create(
                    ContentFactory.Payload(
                    id = UUID.randomUUID(),
                    title = request.title,
                    body = request.body,
                    mediaSourceKey = request.mediaSourceKey,
                    reviewStatus = ReviewStatus.PENDING.name,
                    contentStatus = ContentStatus.DRAFT.name,
                    reviewerId = null,
                    reviewedAt = null,
                    publishedAt = null,
                    dbCreatedAt = now,
                    dbUpdatedAt = now,
                    )
                )
            content.recordDraftCreated()
            Mediator.uow.save()

            return Response(
                contentId = content.id
            )
        }
    }

    data class Request(
        val title: String,
        val body: String,
        val mediaSourceKey: String
    ) : RequestParam<Response>

    data class Response(
        val contentId: UUID
    )

}
