package com.only4.cap4k.reference.contentstudio.adapter.application.queries.content.read

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.query.Query
import com.only4.cap4k.reference.contentstudio.application.queries.content.read.GetMediaProcessingStatusQry
import com.only4.cap4k.reference.contentstudio.domain._share.meta.media_processing_task.SMediaProcessingTask
import org.springframework.stereotype.Service

/**
 * get media processing status
 *
 * 本文件由 cap4k pipeline 生成
 */
@Service
class GetMediaProcessingStatusQryHandler : Query<GetMediaProcessingStatusQry.Request, GetMediaProcessingStatusQry.Response> {

    override fun exec(request: GetMediaProcessingStatusQry.Request): GetMediaProcessingStatusQry.Response {
        val task =
            Mediator.repositories.findFirst(
                SMediaProcessingTask.predicate { schema ->
                    schema.contentId.eq(request.contentId)
                },
                persist = false,
            )
        return GetMediaProcessingStatusQry.Response(
            contentId = request.contentId,
            task =
                task?.let {
                    GetMediaProcessingStatusQry.Response.TaskSnapshot(
                        taskId = it.id,
                        externalTaskId = it.externalTaskId,
                        processingStatus = it.processingStatus.name,
                    )
                }
        )
    }
}
