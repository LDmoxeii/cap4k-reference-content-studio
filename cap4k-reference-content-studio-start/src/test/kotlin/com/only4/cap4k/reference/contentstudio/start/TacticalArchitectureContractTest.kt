package com.only4.cap4k.reference.contentstudio.start

import com.only4.cap4k.ddd.core.domain.service.annotation.DomainService
import com.only4.cap4k.reference.contentstudio.adapter.application.queries.content.read.GetContentDetailQryHandler
import com.only4.cap4k.reference.contentstudio.adapter.application.queries.content.read.GetMediaProcessingStatusQryHandler
import com.only4.cap4k.reference.contentstudio.adapter.application.queries.media.processing.ListSubmittedMediaProcessingTasksForPollingQryHandler
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.ApproveContentReviewCmd
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.CreateContentDraftCmd
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.PublishContentCmd
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.SubmitContentForReviewCmd
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.MarkMediaProcessingSucceededCmd
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.StartMediaProcessingCmd
import com.only4.cap4k.reference.contentstudio.application.subscribers.domain.content.ContentReviewApprovedDomainEventSubscriber
import com.only4.cap4k.reference.contentstudio.application.subscribers.domain.media_processing_task.MediaProcessingSucceededDomainEventSubscriber
import com.only4.cap4k.reference.contentstudio.domain.services.PublicationEligibilityDomainService
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

class TacticalArchitectureContractTest {

    @Test
    fun `command handlers rely on static mediator path instead of injected collaborators or local transactions`() {
        commandHandlers().forEach { type ->
            assertTrue(type.constructors.single().parameterTypes.isEmpty(), "${type.name} should not inject collaborators")
            assertFalse(type.isAnnotationPresent(Transactional::class.java), "${type.name} should not use @Transactional")
        }
        commandSourceFiles().forEach { relativePath ->
            val source = Files.readString(projectRoot().resolve(relativePath))
            assertTrue(source.contains("Mediator.uow.save()"), "$relativePath should commit through Mediator.uow.save()")
        }
    }

    @Test
    fun `query handlers are skip style skeletons without injected repositories`() {
        queryHandlers().forEach { type ->
            assertTrue(type.constructors.single().parameterTypes.isEmpty(), "${type.name} should not inject repositories")
        }
    }

    @Test
    fun `active domain subscribers avoid generic on method names`() {
        activeSubscribers().forEach { type ->
            assertFalse(type.declaredMethods.any { it.name == "on" }, "${type.name} should use semantic consumer method names")
        }
    }

    @Test
    fun `domain service is a domain service bean instead of start-local bean wiring`() {
        assertTrue(
            PublicationEligibilityDomainService::class.java.isAnnotationPresent(DomainService::class.java),
            "PublicationEligibilityDomainService should carry @DomainService",
        )
        assertTrue(
            PublicationEligibilityDomainService::class.java.isAnnotationPresent(Service::class.java),
            "PublicationEligibilityDomainService should be a Spring bean without start-local @Bean wiring",
        )
        assertFalse(
            ContentStudioApplication::class.java.declaredMethods.any { it.name == "publicationEligibilityDomainService" },
            "ContentStudioApplication should not declare a publicationEligibilityDomainService @Bean",
        )
    }

    @Test
    fun `obsolete port and adapter residue is removed from the reference project`() {
        obsoleteClasses().forEach { className ->
            assertClassMissing(className)
        }
    }

    @Test
    fun `polling fallback job reads submitted tasks through query contract instead of direct repository access`() {
        val source =
            Files.readString(
                projectRoot().resolve(
                    "cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/jobs/MediaProcessingPollingFallbackJob.kt",
                ),
            )
        assertFalse(source.contains("Mediator.repositories."), "polling fallback job should not read repositories directly")
        assertTrue(source.contains("Mediator.qry.send("), "polling fallback job should fetch submitted tasks through query contract")
    }

    @Test
    fun `aggregate lifecycle enums move from handwritten source to generated snapshots`() {
        val root = projectRoot()
        handwrittenEnumFiles().forEach { relativePath ->
            assertFalse(Files.exists(root.resolve(relativePath)), "$relativePath should no longer stay handwritten")
        }
        generatedEnumSnapshotFiles().forEach { relativePath ->
            assertTrue(Files.exists(root.resolve(relativePath)), "$relativePath should exist as generated enum snapshot")
        }
    }

    private fun commandHandlers(): List<Class<*>> =
        listOf(
            CreateContentDraftCmd.Handler::class.java,
            SubmitContentForReviewCmd.Handler::class.java,
            ApproveContentReviewCmd.Handler::class.java,
            StartMediaProcessingCmd.Handler::class.java,
            MarkMediaProcessingSucceededCmd.Handler::class.java,
            PublishContentCmd.Handler::class.java,
        )

    private fun queryHandlers(): List<Class<*>> =
        listOf(
            GetContentDetailQryHandler::class.java,
            GetMediaProcessingStatusQryHandler::class.java,
            ListSubmittedMediaProcessingTasksForPollingQryHandler::class.java,
        )

    private fun activeSubscribers(): List<Class<*>> =
        listOf(
            ContentReviewApprovedDomainEventSubscriber::class.java,
            MediaProcessingSucceededDomainEventSubscriber::class.java,
        )

    private fun obsoleteClasses(): List<String> =
        listOf(
            "com.only4.cap4k.reference.contentstudio.application.ports.ContentRepository",
            "com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingTaskRepository",
            "com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingCli",
            "com.only4.cap4k.reference.contentstudio.adapter.persistence.ContentPersistenceAdapter",
            "com.only4.cap4k.reference.contentstudio.adapter.persistence.MediaProcessingTaskPersistenceAdapter",
            "com.only4.cap4k.reference.contentstudio.adapter.application.queries.GetContentDetailQryHandler",
            "com.only4.cap4k.reference.contentstudio.adapter.application.queries.GetCurrentProcessingStatusQryHandler",
            "com.only4.cap4k.reference.contentstudio.application.transition.MediaProcessingSucceededTransitionSurface",
            "com.only4.cap4k.reference.contentstudio.domain.aggregates.content.specification.ContentSpecification",
            "com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.specification.MediaProcessingTaskSpecification",
        )

    private fun handwrittenEnumFiles(): List<String> =
        listOf(
            "cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/ReviewStatus.kt",
            "cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/ContentStatus.kt",
            "cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/MediaProcessingStatus.kt",
        )

    private fun generatedEnumSnapshotFiles(): List<String> =
        listOf(
            "cap4k-reference-content-studio-domain/src-generated/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/enums/ReviewStatus.kt",
            "cap4k-reference-content-studio-domain/src-generated/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/enums/ContentStatus.kt",
            "cap4k-reference-content-studio-domain/src-generated/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/enums/MediaProcessingStatus.kt",
        )

    private fun assertClassMissing(className: String) {
        assertThrows(ClassNotFoundException::class.java, { Class.forName(className) }, "$className should be removed")
    }

    private fun commandSourceFiles(): List<String> =
        listOf(
            "cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/CreateContentDraftCmd.kt",
            "cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/SubmitContentForReviewCmd.kt",
            "cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/ApproveContentReviewCmd.kt",
            "cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/PublishContentCmd.kt",
            "cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/media/processing/StartMediaProcessingCmd.kt",
            "cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/media/processing/MarkMediaProcessingSucceededCmd.kt",
        )

    private fun projectRoot(): Path {
        var current = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        while (!Files.exists(current.resolve("cap4k-reference-content-studio-application"))) {
            current = current.parent ?: error("Failed to locate cap4k-reference-content-studio project root from ${System.getProperty("user.dir")}")
        }
        return current
    }
}
