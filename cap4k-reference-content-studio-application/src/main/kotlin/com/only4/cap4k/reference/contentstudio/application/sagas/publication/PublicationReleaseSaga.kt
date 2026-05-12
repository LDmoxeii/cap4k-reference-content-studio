package com.only4.cap4k.reference.contentstudio.application.sagas.publication

import com.only4.cap4k.ddd.core.application.saga.SagaHandler
import com.only4.cap4k.ddd.core.application.saga.SagaParam
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.PublishContentCmd
import com.only4.cap4k.reference.contentstudio.application.commands.release.readiness.CompletePublicationReleaseReadinessCmd
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object PublicationReleaseSaga {

    const val PROCESS_COMPLETE_RELEASE_READINESS = "complete-release-readiness"
    const val PROCESS_PUBLISH_CONTENT = "publish-content"

    @Service
    class Handler : SagaHandler<Request, Response> {

        override fun exec(request: Request): Response {
            val now = LocalDateTime.now()
            execProcess(
                PROCESS_COMPLETE_RELEASE_READINESS,
                CompletePublicationReleaseReadinessCmd.Request(
                    contentId = request.contentId,
                    completedAt = now,
                )
            )
            execProcess(
                PROCESS_PUBLISH_CONTENT,
                PublishContentCmd.Request(
                    contentId = request.contentId,
                    publishedAt = LocalDateTime.now(),
                    releaseReadinessSatisfied = true,
                )
            )

            return Response()
        }
    }

    data class Request(
        val contentId: UUID
    ) : SagaParam<Response>

    data class Response(
        val published: Boolean = true
    )
}
