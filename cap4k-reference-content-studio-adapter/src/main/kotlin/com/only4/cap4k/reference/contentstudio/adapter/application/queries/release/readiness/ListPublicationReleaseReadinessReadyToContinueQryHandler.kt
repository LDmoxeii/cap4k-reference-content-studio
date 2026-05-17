package com.only4.cap4k.reference.contentstudio.adapter.application.queries.release.readiness

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.ddd.core.application.query.Query
import com.only4.cap4k.reference.contentstudio.application.queries.release.readiness.ListPublicationReleaseReadinessReadyToContinueQry
import com.only4.cap4k.reference.contentstudio.domain._share.meta.publication_release_readiness.SPublicationReleaseReadiness
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.CopyrightReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.ManualReleaseConfirmationStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.PublicationReleaseReadinessState
import org.springframework.stereotype.Service

@Service
class ListPublicationReleaseReadinessReadyToContinueQryHandler :
    Query<
        ListPublicationReleaseReadinessReadyToContinueQry.Request,
        ListPublicationReleaseReadinessReadyToContinueQry.Response
    > {

    override fun exec(
        request: ListPublicationReleaseReadinessReadyToContinueQry.Request
    ): ListPublicationReleaseReadinessReadyToContinueQry.Response {
        val items =
            Mediator.repositories.find(
                SPublicationReleaseReadiness.predicate { schema ->
                    schema.all(
                        schema.readinessState.eq(PublicationReleaseReadinessState.WAITING),
                        schema.copyrightStatus.eq(CopyrightReviewStatus.PASSED),
                        schema.manualConfirmationStatus.eq(ManualReleaseConfirmationStatus.CONFIRMED),
                        schema.releaseWindowOpensAt.le(request.now),
                        schema.releaseWindowClosesAt.ge(request.now),
                        schema.releaseSagaId.isNull(),
                    )
                },
                persist = false,
            ).map { readiness ->
                ListPublicationReleaseReadinessReadyToContinueQry.Response.ReadyItem(
                    contentId = readiness.contentId,
                )
            }

        return ListPublicationReleaseReadinessReadyToContinueQry.Response(items = items)
    }
}
