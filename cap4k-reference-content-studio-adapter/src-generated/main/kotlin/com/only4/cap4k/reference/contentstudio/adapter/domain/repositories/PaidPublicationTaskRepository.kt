package com.only4.cap4k.reference.contentstudio.adapter.domain.repositories

import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.ddd.domain.repo.AbstractJpaRepository
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Repository
interface PaidPublicationTaskRepository : JpaRepository<PaidPublicationTask, UUID>, JpaSpecificationExecutor<PaidPublicationTask> {

    @Component
    @Aggregate(aggregate = "PaidPublicationTask", name = "PaidPublicationTaskRepo", type = Aggregate.TYPE_REPOSITORY, description = "")
    class PaidPublicationTaskJpaRepositoryAdapter(
        jpaSpecificationExecutor: JpaSpecificationExecutor<PaidPublicationTask>,
        jpaRepository: JpaRepository<PaidPublicationTask, UUID>
    ) : AbstractJpaRepository<PaidPublicationTask, UUID>(
        jpaSpecificationExecutor,
        jpaRepository
    )
}
