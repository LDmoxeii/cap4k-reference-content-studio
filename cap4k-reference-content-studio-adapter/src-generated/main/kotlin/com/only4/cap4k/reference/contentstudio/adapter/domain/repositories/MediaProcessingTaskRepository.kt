package com.only4.cap4k.reference.contentstudio.adapter.domain.repositories

import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import com.only4.cap4k.ddd.domain.repo.AbstractJpaRepository
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Repository
interface MediaProcessingTaskRepository : JpaRepository<MediaProcessingTask, UUID>, JpaSpecificationExecutor<MediaProcessingTask> {

    @Component
    @Aggregate(aggregate = "MediaProcessingTask", name = "MediaProcessingTaskRepo", type = Aggregate.TYPE_REPOSITORY, description = "")
    class MediaProcessingTaskJpaRepositoryAdapter(
        jpaSpecificationExecutor: JpaSpecificationExecutor<MediaProcessingTask>,
        jpaRepository: JpaRepository<MediaProcessingTask, UUID>
    ) : AbstractJpaRepository<MediaProcessingTask, UUID>(
        jpaSpecificationExecutor,
        jpaRepository
    )
}
