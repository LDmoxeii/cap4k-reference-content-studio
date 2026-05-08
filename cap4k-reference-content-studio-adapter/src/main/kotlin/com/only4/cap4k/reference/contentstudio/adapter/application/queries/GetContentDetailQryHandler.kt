package com.only4.cap4k.reference.contentstudio.adapter.application.queries

import com.only4.cap4k.ddd.core.application.query.Query
import com.only4.cap4k.reference.contentstudio.application.ports.ContentRepository
import com.only4.cap4k.reference.contentstudio.application.queries.content.read.GetContentDetailQry
import org.springframework.stereotype.Service

@Service
class GetContentDetailQryHandler(
    private val contentRepository: ContentRepository,
) : Query<GetContentDetailQry.Request, GetContentDetailQry.Response> {

    override fun exec(request: GetContentDetailQry.Request): GetContentDetailQry.Response {
        val content = checkNotNull(contentRepository.findById(request.contentId)) {
            "Content ${request.contentId} was not found."
        }

        return GetContentDetailQry.Response(
            contentId = content.id,
            title = content.title,
            body = content.body,
            mediaSourceKey = content.mediaSourceKey,
            reviewStatus = content.reviewStatus,
            contentStatus = content.contentStatus,
            reviewerId = content.reviewerId,
            reviewedAt = content.reviewedAt,
            publishedAt = content.publishedAt,
        )
    }
}
