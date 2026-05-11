package com.only4.cap4k.reference.contentstudio.application.commands.media.processing

import com.only4.cap4k.ddd.core.application.UnitOfWork
import com.only4.cap4k.ddd.core.application.UnitOfWorkSupport
import com.only4.cap4k.ddd.core.domain.repo.Predicate
import com.only4.cap4k.ddd.core.domain.repo.RepositorySupervisor
import com.only4.cap4k.ddd.core.domain.repo.RepositorySupervisorSupport
import com.only4.cap4k.ddd.core.share.OrderInfo
import com.only4.cap4k.ddd.core.share.PageData
import com.only4.cap4k.ddd.core.share.PageParam
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.enums.MediaProcessingStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Propagation
import java.time.LocalDateTime
import java.util.UUID

class MarkMediaProcessingSucceededCmdTest {
    @Test
    fun `already succeeded task returns without persisting duplicate result snapshot`() {
        val task = newTask(processingStatus = MediaProcessingStatus.SUCCEEDED)
        val repositorySupervisor = StubRepositorySupervisor(task)
        val unitOfWork = RecordingUnitOfWork()
        RepositorySupervisorSupport.configure(repositorySupervisor)
        UnitOfWorkSupport.configure(unitOfWork)

        MarkMediaProcessingSucceededCmd.Handler().exec(
            MarkMediaProcessingSucceededCmd.Request(
                externalTaskId = "external-123",
                assetSha256 = "a".repeat(64),
                assetLocation = "s3://content-studio/assets/external-123.mp4",
                completedAt = LocalDateTime.parse("2026-05-11T10:15:30"),
            )
        )

        assertEquals(0, unitOfWork.persistedEntities.size)
        assertEquals(0, unitOfWork.saveCalls)
    }

    private fun newTask(
        externalTaskId: String? = "external-123",
        processingStatus: MediaProcessingStatus,
    ): MediaProcessingTask {
        val now = LocalDateTime.of(2026, 5, 9, 9, 0)
        return MediaProcessingTask(
            id = UUID.randomUUID(),
            contentId = UUID.randomUUID(),
            externalTaskId = externalTaskId,
            processingStatus = processingStatus,
            dbCreatedAt = now,
            dbUpdatedAt = now,
        )
    }

    private class StubRepositorySupervisor(
        private val task: MediaProcessingTask,
    ) : RepositorySupervisor {
        @Suppress("UNCHECKED_CAST")
        override fun <ENTITY : Any> findFirst(
            predicate: Predicate<ENTITY>,
            orders: Collection<OrderInfo>,
            persist: Boolean,
        ): ENTITY? = task as ENTITY

        override fun <ENTITY : Any> find(
            predicate: Predicate<ENTITY>,
            orders: Collection<OrderInfo>,
            persist: Boolean,
        ): List<ENTITY> = unsupported()

        override fun <ENTITY : Any> find(
            predicate: Predicate<ENTITY>,
            pageParam: PageParam,
            persist: Boolean,
        ): List<ENTITY> = unsupported()

        override fun <ENTITY : Any> findOne(predicate: Predicate<ENTITY>, persist: Boolean): ENTITY? = unsupported()

        override fun <ENTITY : Any> findPage(
            predicate: Predicate<ENTITY>,
            pageParam: PageParam,
            persist: Boolean,
        ): PageData<ENTITY> = unsupported()

        override fun <ENTITY : Any> remove(predicate: Predicate<ENTITY>): List<ENTITY> = unsupported()

        override fun <ENTITY : Any> remove(predicate: Predicate<ENTITY>, limit: Int): List<ENTITY> = unsupported()

        override fun <ENTITY : Any> count(predicate: Predicate<ENTITY>): Long = unsupported()

        override fun <ENTITY : Any> exists(predicate: Predicate<ENTITY>): Boolean = unsupported()

        private fun unsupported(): Nothing = error("This repository method is not used by this test.")
    }

    private class RecordingUnitOfWork : UnitOfWork {
        val persistedEntities = mutableListOf<Any>()
        var saveCalls = 0

        override fun persist(entity: Any) {
            persistedEntities += entity
        }

        override fun persistIfNotExist(entity: Any): Boolean {
            persistedEntities += entity
            return true
        }

        override fun remove(entity: Any) = Unit

        override fun save(propagation: Propagation) {
            saveCalls += 1
        }
    }
}
