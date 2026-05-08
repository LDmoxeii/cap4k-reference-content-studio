package com.only4.cap4k.reference.contentstudio.domain.aggregates.content.specification

import com.only4.cap4k.ddd.core.domain.aggregate.Specification
import com.only4.cap4k.ddd.core.domain.aggregate.Specification.Result
import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import org.springframework.stereotype.Service

@Service
@Aggregate(
    aggregate = "Content",
    name = "ContentSpecification",
    type = Aggregate.TYPE_SPECIFICATION,
    description = ""
)
class ContentSpecification : Specification<Content> {

    override fun specify(entity: Content): Result {
        return Result.pass()
    }
}
