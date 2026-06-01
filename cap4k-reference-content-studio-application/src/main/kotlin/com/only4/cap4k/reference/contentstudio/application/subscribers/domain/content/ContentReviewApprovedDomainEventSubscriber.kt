package com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content

import org.springframework.stereotype.Service
import com.only4.cap4k.ddd.core.annotation.BuildingBlock

/**
 * content review approved
 */
@Service
@BuildingBlock(
    tag = "domain_event",
    name = "ContentReviewApproved",
    packageName = "content",
    description = "content review approved",
    aggregates = ["Content"],
    family = "domain-subscriber"
)
class ContentReviewApprovedDomainEventSubscriber
