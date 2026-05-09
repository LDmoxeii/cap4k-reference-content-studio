package com.only4.cap4k.reference.contentstudio.adapter.application.queries.content.read

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.query.Query
import com.only4.cap4k.reference.contentstudio.application.queries.content.read.GetContentDetailQry
import com.only4.cap4k.reference.contentstudio.domain._share.meta.content.SContent
import org.springframework.stereotype.Service

/**
 * get content detail
 *
 * 本文件由 cap4k pipeline 生成
 */
@Service
class GetContentDetailQryHandler : Query<GetContentDetailQry.Request, GetContentDetailQry.Response> {

    override fun exec(request: GetContentDetailQry.Request): GetContentDetailQry.Response {
        val content =
            checkNotNull(
                Mediator.repositories.findOne(
                    SContent.predicateById(request.contentId),
                    persist = false,
                )
            ) {
                "Content ${request.contentId} was not found."
            }
        return GetContentDetailQry.Response(
            contentId = content.id,
            title = content.title,
            body = content.body,
            mediaSourceKey = content.mediaSourceKey,
            reviewStatus = content.reviewStatus.name,
            contentStatus = content.contentStatus.name,
            reviewerId = content.reviewerId,
            reviewedAt = content.reviewedAt,
            publishedAt = content.publishedAt
        )
    }
}
