package com.only4.cap4k.reference.contentstudio.adapter.application.queries

import com.only4.cap4k.ddd.core.application.query.Query
import com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingTaskRepository
import com.only4.cap4k.reference.contentstudio.application.queries.content.read.GetMediaProcessingStatusQry
import org.springframework.stereotype.Service

@Service
class GetCurrentProcessingStatusQryHandler(
    private val mediaProcessingTaskRepository: MediaProcessingTaskRepository,
) : Query<GetMediaProcessingStatusQry.Request, GetMediaProcessingStatusQry.Response> {

    override fun exec(request: GetMediaProcessingStatusQry.Request): GetMediaProcessingStatusQry.Response {
        val task = mediaProcessingTaskRepository.findByContentId(request.contentId)

        return GetMediaProcessingStatusQry.Response(
            contentId = request.contentId,
            task = task?.let {
                GetMediaProcessingStatusQry.Response.TaskSnapshot(
                    taskId = it.id,
                    externalTaskId = it.externalTaskId,
                    processingStatus = it.processingStatus,
                )
            },
        )
    }
}
