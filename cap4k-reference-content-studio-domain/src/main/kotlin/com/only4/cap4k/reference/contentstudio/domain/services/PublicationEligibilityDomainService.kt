package com.only4.cap4k.reference.contentstudio.domain.services

import com.only4.cap4k.ddd.core.domain.service.annotation.DomainService
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import org.springframework.stereotype.Service

@DomainService
@Service
class PublicationEligibilityDomainService {
    fun evaluate(
        content: Content,
        task: MediaProcessingTask,
        releaseReadinessSatisfied: Boolean = true,
    ): PublicationEligibilityDecision {
        return when {
            task.contentId != content.id -> PublicationEligibilityDecision.TaskDoesNotBelongToContent
            content.reviewStatus != ReviewStatus.APPROVED -> PublicationEligibilityDecision.ContentNotApproved
            task.processingStatus != MediaProcessingStatus.SUCCEEDED ->
                PublicationEligibilityDecision.MediaProcessingNotSucceeded
            !releaseReadinessSatisfied -> PublicationEligibilityDecision.ReleaseReadinessNotSatisfied
            else -> PublicationEligibilityDecision.Eligible
        }
    }
}

sealed interface PublicationEligibilityDecision {
    data object Eligible : PublicationEligibilityDecision
    data object ContentNotApproved : PublicationEligibilityDecision
    data object MediaProcessingNotSucceeded : PublicationEligibilityDecision
    data object TaskDoesNotBelongToContent : PublicationEligibilityDecision
    data object ReleaseReadinessNotSatisfied : PublicationEligibilityDecision
}
