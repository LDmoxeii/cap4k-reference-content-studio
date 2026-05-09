package com.only4.cap4k.reference.contentstudio.adapter.application.queries.content.read

import com.only4.cap4k.ddd.core.application.query.Query
import com.only4.cap4k.reference.contentstudio.application.queries.content.read.GetContentDetailQry
import org.springframework.stereotype.Service

/**
 * get content detail
 *
 * 本文件由 cap4k pipeline 生成
 */
@Service
class GetContentDetailQryHandler : Query<GetContentDetailQry.Request, GetContentDetailQry.Response> {

    override fun exec(request: GetContentDetailQry.Request): GetContentDetailQry.Response {
        return GetContentDetailQry.Response(
            contentId = TODO("set contentId"),
            title = TODO("set title"),
            body = TODO("set body"),
            mediaSourceKey = TODO("set mediaSourceKey"),
            reviewStatus = TODO("set reviewStatus"),
            contentStatus = TODO("set contentStatus"),
            reviewerId = TODO("set reviewerId"),
            reviewedAt = TODO("set reviewedAt"),
            publishedAt = TODO("set publishedAt")
        )
    }
}
