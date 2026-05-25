package com.only4.cap4k.reference.contentstudio.application.commands.content.workflow

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.factory.ContentFactory
import java.time.LocalDateTime
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentId
import org.springframework.stereotype.Service

object CreatePaidContentDraftCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val now = LocalDateTime.now()
            val content =
                Mediator.factories.create(
                    ContentFactory.Payload(
                        title = request.title,
                        body = request.body,
                        mediaSourceKey = request.mediaSourceKey,
                        reviewStatus = ReviewStatus.PENDING,
                        contentStatus = ContentStatus.DRAFT,
                        releasePolicy = ReleasePolicy.PAID,
                        reviewerId = null,
                        reviewedAt = null,
                        publishedAt = null,
                        dbCreatedAt = now,
                        dbUpdatedAt = now,
                    )
                )
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
        val contentId: ContentId
    )

}
