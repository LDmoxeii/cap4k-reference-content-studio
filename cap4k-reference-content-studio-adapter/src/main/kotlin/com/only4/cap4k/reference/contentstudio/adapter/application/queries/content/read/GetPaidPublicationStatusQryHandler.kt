package com.only4.cap4k.reference.contentstudio.adapter.application.queries.content.read

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.query.Query
import com.only4.cap4k.reference.contentstudio.application.queries.content.read.GetPaidPublicationStatusQry
import com.only4.cap4k.reference.contentstudio.domain._share.meta.paid_publication_task.SPaidPublicationTask
import org.springframework.stereotype.Service

/**
 * get paid publication status
 *
 * 本文件由 cap4k pipeline 生成
 */
@Service
class GetPaidPublicationStatusQryHandler :
    Query<GetPaidPublicationStatusQry.Request, GetPaidPublicationStatusQry.Response> {

    override fun exec(request: GetPaidPublicationStatusQry.Request): GetPaidPublicationStatusQry.Response {
        val task =
            Mediator.repositories.findFirst(
                SPaidPublicationTask.predicate { schema -> schema.contentId.eq(request.contentId) },
                persist = false,
            )

        return GetPaidPublicationStatusQry.Response(
            contentId = request.contentId,
            taskId = task?.id,
            paidPublicationStatus = task?.paidPublicationStatus?.name,
            payoutHoldStatus = task?.payoutHoldStatus?.name,
            entitlementPlanStatus = task?.entitlementPlanStatus?.name,
            startedAt = task?.startedAt,
            publishedAt = task?.publishedAt,
            completedAt = task?.completedAt,
            failedAt = task?.failedAt,
            failedReason = task?.failedReason,
        )
    }
}
