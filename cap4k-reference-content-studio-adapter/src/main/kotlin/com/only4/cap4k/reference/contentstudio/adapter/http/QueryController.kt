package com.only4.cap4k.reference.contentstudio.adapter.http

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.read.GetContentDetailPayload
import com.only4.cap4k.reference.contentstudio.adapter.portal.api.payload.content.read.GetMediaProcessingStatusPayload
import com.only4.cap4k.reference.contentstudio.application.queries.content.read.GetContentDetailQry
import com.only4.cap4k.reference.contentstudio.application.queries.content.read.GetMediaProcessingStatusQry
import java.util.UUID
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class QueryController {

    @GetMapping("/contents/{contentId}")
    fun getContent(@PathVariable contentId: UUID): GetContentDetailPayload.Response {
        val response = Mediator.qry.send(GetContentDetailQry.Request(contentId = contentId))

        return GetContentDetailPayload.Response(
            contentId = response.contentId,
            title = response.title,
            body = response.body,
            mediaSourceKey = response.mediaSourceKey,
            reviewStatus = response.reviewStatus,
            contentStatus = response.contentStatus,
            reviewerId = response.reviewerId,
            reviewedAt = response.reviewedAt,
            publishedAt = response.publishedAt,
        )
    }

    @GetMapping("/media-processing/{contentId}")
    fun getMediaProcessing(@PathVariable contentId: UUID): GetMediaProcessingStatusPayload.Response {
        val response = Mediator.qry.send(GetMediaProcessingStatusQry.Request(contentId = contentId))

        return GetMediaProcessingStatusPayload.Response(
            contentId = response.contentId,
            task =
                response.task?.let { task ->
                    GetMediaProcessingStatusPayload.Response.Task(
                        taskId = task.taskId,
                        externalTaskId = task.externalTaskId,
                        processingStatus = task.processingStatus,
                    )
                },
        )
    }
}
