package com.only4.cap4k.reference.contentstudio.adapter.persistence

import com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingTaskRepository
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import jakarta.persistence.EntityManager
import java.util.UUID
import org.springframework.stereotype.Repository
import com.only4.cap4k.reference.contentstudio.adapter.domain.repositories.MediaProcessingTaskRepository as JpaMediaProcessingTaskRepository

@Repository
class MediaProcessingTaskPersistenceAdapter(
    private val jpaMediaProcessingTaskRepository: JpaMediaProcessingTaskRepository,
    private val entityManager: EntityManager,
) : MediaProcessingTaskRepository {

    override fun findByContentId(contentId: UUID): MediaProcessingTask? =
        entityManager.createQuery(
            """
            select task
            from MediaProcessingTask task
            where task.contentId = :contentId
            """.trimIndent(),
            MediaProcessingTask::class.java,
        )
            .setParameter("contentId", contentId)
            .resultList
            .firstOrNull()

    override fun findByExternalTaskId(externalTaskId: String): MediaProcessingTask? =
        entityManager.createQuery(
            """
            select task
            from MediaProcessingTask task
            where task.externalTaskId = :externalTaskId
            """.trimIndent(),
            MediaProcessingTask::class.java,
        )
            .setParameter("externalTaskId", externalTaskId)
            .resultList
            .firstOrNull()

    override fun save(task: MediaProcessingTask): MediaProcessingTask =
        jpaMediaProcessingTaskRepository.save(task)
}
