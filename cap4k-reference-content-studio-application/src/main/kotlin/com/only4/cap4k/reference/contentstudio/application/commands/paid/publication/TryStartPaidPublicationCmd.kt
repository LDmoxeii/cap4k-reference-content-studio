package com.only4.cap4k.reference.contentstudio.application.commands.paid.publication

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.command.Command
import com.only4.cap4k.reference.contentstudio.application.sagas.paid.publication.PaidPublicationSaga
import com.only4.cap4k.reference.contentstudio.domain._share.meta.content.SContent
import com.only4.cap4k.reference.contentstudio.domain._share.meta.paid_publication_task.SPaidPublicationTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.factory.PaidPublicationTaskFactory
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.recordSagaStarted
import com.only4.cap4k.reference.contentstudio.domain.services.paid.publication.PaidPublicationEligibilityService
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service

object TryStartPaidPublicationCmd {

    @Service
    class Handler : Command<Request, Response> {

        override fun exec(request: Request): Response {
            val content = checkNotNull(Mediator.repositories.findOne(SContent.predicateById(request.contentId))) {
                "Content ${request.contentId} was not found."
            }
            val existingTask =
                Mediator.repositories.findFirst(
                    SPaidPublicationTask.predicate { schema ->
                        schema.contentId.eq(request.contentId)
                    },
                    persist = true
                )

            val eligibilityService =
                Mediator.services.getService(PaidPublicationEligibilityService::class.java)
            return when (eligibilityService.decide(content, existingTask)) {
                PaidPublicationEligibilityService.Decision.Eligible -> {
                    val now = LocalDateTime.now()
                    val task =
                        existingTask ?: Mediator.factories.create(
                            PaidPublicationTaskFactory.Payload(
                                id = UUID.randomUUID(),
                                contentId = request.contentId,
                                now = now,
                            )
                        )
                    val sagaId =
                        Mediator.requests.schedule(
                            PaidPublicationSaga.Request(
                                paidPublicationTaskId = task.id,
                            ),
                            now.plusSeconds(1),
                        )
                    task.recordSagaStarted(sagaId, now)
                    Mediator.uow.save()

                    Response(taskId = task.id, started = true)
                }

                PaidPublicationEligibilityService.Decision.AlreadyStarted ->
                    Response(taskId = existingTask?.id, started = false)

                else ->
                    Response(taskId = existingTask?.id, started = false)
            }
        }
    }

    data class Request(
        val contentId: UUID
    ) : RequestParam<Response>

    data class Response(
        val taskId: UUID?,
        val started: Boolean
    )

}
