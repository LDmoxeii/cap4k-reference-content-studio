package com.only4.cap4k.reference.contentstudio.adapter.persistence

import com.only4.cap4k.ddd.core.domain.event.DomainEventSupervisor
import com.only4.cap4k.reference.contentstudio.application.ports.ContentRepository
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import java.util.UUID
import org.springframework.stereotype.Repository
import com.only4.cap4k.reference.contentstudio.adapter.domain.repositories.ContentRepository as JpaContentRepository

@Repository
class ContentPersistenceAdapter(
    private val jpaContentRepository: JpaContentRepository,
) : ContentRepository {

    override fun findById(id: UUID): Content? = jpaContentRepository.findById(id).orElse(null)

    override fun save(content: Content): Content =
        jpaContentRepository.save(content).also {
            DomainEventSupervisor.manager.release(setOf(content))
        }
}
