package com.only4.cap4k.reference.contentstudio.adapter.domain.repositories

import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.ddd.domain.repo.AbstractJpaRepository
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.PublicationReleaseReadiness
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Repository
interface PublicationReleaseReadinessRepository : JpaRepository<PublicationReleaseReadiness, UUID>, JpaSpecificationExecutor<PublicationReleaseReadiness> {

    @Component
    @Aggregate(aggregate = "PublicationReleaseReadiness", name = "PublicationReleaseReadinessRepo", type = Aggregate.TYPE_REPOSITORY, description = "")
    class PublicationReleaseReadinessJpaRepositoryAdapter(
        jpaSpecificationExecutor: JpaSpecificationExecutor<PublicationReleaseReadiness>,
        jpaRepository: JpaRepository<PublicationReleaseReadiness, UUID>
    ) : AbstractJpaRepository<PublicationReleaseReadiness, UUID>(
        jpaSpecificationExecutor,
        jpaRepository
    )
}
