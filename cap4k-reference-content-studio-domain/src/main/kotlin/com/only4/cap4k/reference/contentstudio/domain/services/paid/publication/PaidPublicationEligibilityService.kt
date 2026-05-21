package com.only4.cap4k.reference.contentstudio.domain.services.paid.publication

import com.only4.cap4k.ddd.core.domain.service.annotation.DomainService
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.isReadyForPaidPublication
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import org.springframework.stereotype.Service

@Service
@DomainService(
    name = "PaidPublicationEligibilityService",
    description = "Decides whether paid content can start paid publication."
)
class PaidPublicationEligibilityService {

    fun decide(content: Content, existingTask: PaidPublicationTask?): Decision =
        when {
            content.releasePolicy != ReleasePolicy.PAID -> Decision.NotPaidContent
            content.contentStatus == ContentStatus.PUBLISHED -> Decision.AlreadyPublished
            !content.isReadyForPaidPublication() -> Decision.NotPublicationReady
            existingTask?.publicationSagaId != null -> Decision.AlreadyStarted
            else -> Decision.Eligible
        }

    enum class Decision {
        Eligible,
        NotPaidContent,
        NotPublicationReady,
        AlreadyStarted,
        AlreadyPublished,
    }
}
