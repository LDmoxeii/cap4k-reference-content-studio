package com.only4.cap4k.reference.contentstudio.application.jobs

import com.only4.cap4k.ddd.core.Mediator
import com.only4.cap4k.reference.contentstudio.application.commands.release.readiness.TryContinuePublicationReleaseCmd
import com.only4.cap4k.reference.contentstudio.application.queries.release.readiness.ListPublicationReleaseReadinessReadyToContinueQry
import java.time.LocalDateTime
import org.springframework.stereotype.Service

/**
 * Release windows can open after all human gates are already satisfied.
 * This job routes those now-ready records back to the normal continuation command.
 */
@Service
class PublicationReleaseContinuationJob {

    fun continueReadyReleases() {
        val readyItems =
            Mediator.qry.send(
                ListPublicationReleaseReadinessReadyToContinueQry.Request(
                    now = LocalDateTime.now(),
                )
            ).items

        for (item in readyItems) {
            Mediator.cmd.send(
                TryContinuePublicationReleaseCmd.Request(
                    contentId = item.contentId,
                )
            )
        }
    }
}
