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
    fun evaluate(content: Content, task: MediaProcessingTask): Boolean {
        return task.contentId == content.id &&
            content.reviewStatus == ReviewStatus.APPROVED &&
            task.processingStatus == MediaProcessingStatus.SUCCEEDED
    }
}
