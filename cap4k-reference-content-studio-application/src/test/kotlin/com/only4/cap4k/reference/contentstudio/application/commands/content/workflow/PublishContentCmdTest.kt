package com.only4.cap4k.reference.contentstudio.application.commands.content.workflow

import com.only4.cap4k.ddd.core.application.UnitOfWork
import com.only4.cap4k.ddd.core.application.UnitOfWorkSupport
import com.only4.cap4k.ddd.core.domain.event.DomainEventSupervisor
import com.only4.cap4k.ddd.core.domain.event.DomainEventSupervisorSupport
import com.only4.cap4k.ddd.core.domain.repo.Predicate
import com.only4.cap4k.ddd.core.domain.repo.RepositorySupervisor
import com.only4.cap4k.ddd.core.domain.repo.RepositorySupervisorSupport
import com.only4.cap4k.ddd.core.domain.service.DomainServiceSupervisor
import com.only4.cap4k.ddd.core.domain.service.DomainServiceSupervisorSupport
import com.only4.cap4k.ddd.core.share.OrderInfo
import com.only4.cap4k.ddd.core.share.PageData
import com.only4.cap4k.ddd.core.share.PageParam
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReleasePolicy
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.services.PublicationEligibilityDomainService
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Propagation

class PublishContentCmdTest {

    private lateinit var content: Content
    private lateinit var task: MediaProcessingTask

    @BeforeEach
    fun configureMediator() {
        val contentId = UUID.randomUUID()
        content =
            newContent(
                contentId = contentId,
                reviewStatus = ReviewStatus.APPROVED,
                releasePolicy = ReleasePolicy.GATED,
            )
        task = newTask(contentId = contentId, processingStatus = MediaProcessingStatus.SUCCEEDED)

        RepositorySupervisorSupport.configure(StubRepositorySupervisor { content to task })
        DomainServiceSupervisorSupport.configure(StubDomainServiceSupervisor())
        UnitOfWorkSupport.configure(StubUnitOfWork())
        DomainEventSupervisorSupport.configure(StubDomainEventSupervisor())
    }

    @Test
    fun `gated content cannot publish without release readiness fact`() {
        assertThrows(IllegalStateException::class.java) {
            PublishContentCmd.Handler().exec(
                PublishContentCmd.Request(
                    contentId = content.id,
                    publishedAt = LocalDateTime.of(2026, 5, 11, 10, 0),
                )
            )
        }
    }

    private fun newContent(
        contentId: UUID,
        reviewStatus: ReviewStatus,
        releasePolicy: ReleasePolicy,
    ): Content {
        val now = LocalDateTime.of(2026, 5, 11, 9, 0)
        return Content(
            id = contentId,
            title = "Draft title",
            body = "Draft body",
            mediaSourceKey = "media/source-key",
            reviewStatus = reviewStatus,
            contentStatus = ContentStatus.DRAFT,
            releasePolicy = releasePolicy,
            releaseWindowOpensAt = now.plusHours(1),
            releaseWindowClosesAt = now.plusHours(2),
            reviewerId = UUID.randomUUID(),
            reviewedAt = now,
            publishedAt = null,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }

    private fun newTask(contentId: UUID, processingStatus: MediaProcessingStatus): MediaProcessingTask {
        val now = LocalDateTime.of(2026, 5, 11, 9, 30)
        return MediaProcessingTask(
            id = UUID.randomUUID(),
            contentId = contentId,
            externalTaskId = "external-123",
            processingStatus = processingStatus,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }

    private class StubRepositorySupervisor(
        private val state: () -> Pair<Content, MediaProcessingTask>,
    ) : RepositorySupervisor {
        override fun <ENTITY : Any> find(
            predicate: Predicate<ENTITY>,
            orders: Collection<OrderInfo>,
            persist: Boolean,
        ): List<ENTITY> = throw UnsupportedOperationException()

        override fun <ENTITY : Any> find(
            predicate: Predicate<ENTITY>,
            pageParam: PageParam,
            persist: Boolean,
        ): List<ENTITY> = throw UnsupportedOperationException()

        override fun <ENTITY : Any> findOne(predicate: Predicate<ENTITY>, persist: Boolean): ENTITY? {
            @Suppress("UNCHECKED_CAST")
            return state().first as ENTITY
        }

        override fun <ENTITY : Any> findFirst(
            predicate: Predicate<ENTITY>,
            orders: Collection<OrderInfo>,
            persist: Boolean,
        ): ENTITY? {
            @Suppress("UNCHECKED_CAST")
            return state().second as ENTITY
        }

        override fun <ENTITY : Any> findPage(
            predicate: Predicate<ENTITY>,
            pageParam: PageParam,
            persist: Boolean,
        ): PageData<ENTITY> = throw UnsupportedOperationException()

        override fun <ENTITY : Any> remove(predicate: Predicate<ENTITY>): List<ENTITY> =
            throw UnsupportedOperationException()

        override fun <ENTITY : Any> remove(predicate: Predicate<ENTITY>, limit: Int): List<ENTITY> =
            throw UnsupportedOperationException()

        override fun <ENTITY : Any> count(predicate: Predicate<ENTITY>): Long =
            throw UnsupportedOperationException()

        override fun <ENTITY : Any> exists(predicate: Predicate<ENTITY>): Boolean =
            throw UnsupportedOperationException()
    }

    private class StubDomainServiceSupervisor : DomainServiceSupervisor {
        override fun <DOMAIN_SERVICE : Any> getService(domainServiceClass: Class<DOMAIN_SERVICE>): DOMAIN_SERVICE {
            @Suppress("UNCHECKED_CAST")
            return PublicationEligibilityDomainService() as DOMAIN_SERVICE
        }
    }

    private class StubUnitOfWork : UnitOfWork {
        override fun persist(entity: Any) = Unit

        override fun persistIfNotExist(entity: Any): Boolean = true

        override fun remove(entity: Any) = Unit

        override fun save(propagation: Propagation) = Unit
    }

    private class StubDomainEventSupervisor : DomainEventSupervisor {
        override fun <DOMAIN_EVENT : Any, ENTITY : Any> attach(
            domainEventPayload: DOMAIN_EVENT,
            entity: ENTITY,
            schedule: LocalDateTime,
        ) = Unit

        override fun <DOMAIN_EVENT : Any, ENTITY : Any> attach(
            entity: ENTITY,
            schedule: LocalDateTime,
            domainEventPayloadSupplier: () -> DOMAIN_EVENT,
        ) = Unit

        override fun <DOMAIN_EVENT : Any, ENTITY : Any> detach(domainEventPayload: DOMAIN_EVENT, entity: ENTITY) = Unit
    }
}
