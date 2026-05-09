package com.only4.cap4k.reference.contentstudio.adapter.domain.repositories

import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.ddd.domain.repo.AbstractJpaRepository
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Repository
interface ContentRepository : JpaRepository<Content, UUID>, JpaSpecificationExecutor<Content> {

    @Component
    @Aggregate(aggregate = "Content", name = "ContentRepo", type = Aggregate.TYPE_REPOSITORY, description = "")
    class ContentJpaRepositoryAdapter(
        jpaSpecificationExecutor: JpaSpecificationExecutor<Content>,
        jpaRepository: JpaRepository<Content, UUID>
    ) : AbstractJpaRepository<Content, UUID>(
        jpaSpecificationExecutor,
        jpaRepository
    )
}
