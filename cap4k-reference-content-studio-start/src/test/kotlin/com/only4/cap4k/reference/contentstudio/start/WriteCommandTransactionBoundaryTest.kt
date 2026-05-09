package com.only4.cap4k.reference.contentstudio.start

import com.only4.cap4k.reference.contentstudio.adapter.persistence.ContentPersistenceAdapter
import com.only4.cap4k.reference.contentstudio.adapter.integration.FakeMediaProcessingCli
import com.only4.cap4k.reference.contentstudio.adapter.persistence.MediaProcessingTaskPersistenceAdapter
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.ApproveContentReviewCmd
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.CreateContentDraftCmd
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.PublishContentCmd
import com.only4.cap4k.reference.contentstudio.application.commands.content.workflow.SubmitContentForReviewCmd
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.MarkMediaProcessingSucceededCmd
import com.only4.cap4k.reference.contentstudio.application.commands.media.processing.StartMediaProcessingCmd
import com.only4.cap4k.reference.contentstudio.application.ports.ContentRepository
import com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingCli
import com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingTaskRepository
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import com.only4.cap4k.reference.contentstudio.domain.services.PublicationEligibilityDomainService
import jakarta.persistence.EntityManager
import java.time.LocalDateTime
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.support.TransactionSynchronizationManager

@SpringBootTest(
    classes = [WriteCommandTransactionBoundaryTest.TransactionTestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = [
        "spring.datasource.url=jdbc:h2:mem:content-studio-write-commands;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
    ],
)
class WriteCommandTransactionBoundaryTest
@Autowired
constructor(
    private val createContentDraftHandler: CreateContentDraftCmd.Handler,
    private val submitContentForReviewHandler: SubmitContentForReviewCmd.Handler,
    private val approveContentReviewHandler: ApproveContentReviewCmd.Handler,
    private val startMediaProcessingHandler: StartMediaProcessingCmd.Handler,
    private val markMediaProcessingSucceededHandler: MarkMediaProcessingSucceededCmd.Handler,
    private val publishContentHandler: PublishContentCmd.Handler,
    private val saveTransactionProbe: SaveTransactionProbe,
) {

    @Test
    fun `write command handlers keep aggregate saves inside an active transaction`() {
        val createResponse =
            createContentDraftHandler.exec(
                CreateContentDraftCmd.Request(
                    title = "Transactional path",
                    body = "Exercise the handler transaction boundary",
                    mediaSourceKey = "media/tx-boundary.mp4",
                )
            )
        val contentId = createResponse.contentId

        submitContentForReviewHandler.exec(
            SubmitContentForReviewCmd.Request(contentId = contentId)
        )
        approveContentReviewHandler.exec(
            ApproveContentReviewCmd.Request(
                contentId = contentId,
                reviewerId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                reviewedAt = LocalDateTime.of(2026, 5, 9, 12, 0),
            )
        )
        startMediaProcessingHandler.exec(
            StartMediaProcessingCmd.Request(contentId = contentId)
        )
        markMediaProcessingSucceededHandler.exec(
            MarkMediaProcessingSucceededCmd.Request(externalTaskId = "fake-media-$contentId")
        )
        publishContentHandler.exec(
            PublishContentCmd.Request(
                contentId = contentId,
                publishedAt = LocalDateTime.of(2026, 5, 9, 12, 5),
            )
        )

        assertThat(saveTransactionProbe.records)
            .hasSize(6)
            .allSatisfy { record ->
                assertThat(record.transactionActive)
                    .describedAs("Expected active transaction for %s save on %s", record.aggregateType, record.aggregateId)
                    .isTrue()
            }
    }

    data class SaveTransactionRecord(
        val aggregateType: String,
        val aggregateId: String,
        val transactionActive: Boolean,
    )

    class SaveTransactionProbe {
        private val _records = mutableListOf<SaveTransactionRecord>()
        val records: List<SaveTransactionRecord>
            get() = _records.toList()

        fun record(aggregateType: String, aggregateId: String) {
            _records +=
                SaveTransactionRecord(
                    aggregateType = aggregateType,
                    aggregateId = aggregateId,
                    transactionActive = TransactionSynchronizationManager.isActualTransactionActive(),
                )
        }
    }

    @SpringBootConfiguration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EntityScan(basePackages = ["com.only4.cap4k.reference.contentstudio.domain.aggregates"])
    @EnableJpaRepositories(basePackages = ["com.only4.cap4k.reference.contentstudio.adapter.domain.repositories"])
    @Import(TransactionHandlersConfiguration::class, TransactionProbeConfiguration::class)
    class TransactionTestApplication

    @TestConfiguration(proxyBeanMethods = false)
    class TransactionHandlersConfiguration {
        @Bean
        fun mediaProcessingCli(): MediaProcessingCli = FakeMediaProcessingCli()

        @Bean
        fun publicationEligibilityDomainService(): PublicationEligibilityDomainService =
            PublicationEligibilityDomainService()

        @Bean
        fun contentPersistenceAdapter(
            jpaContentRepository: com.only4.cap4k.reference.contentstudio.adapter.domain.repositories.ContentRepository,
        ): ContentPersistenceAdapter =
            ContentPersistenceAdapter(jpaContentRepository)

        @Bean
        fun mediaProcessingTaskPersistenceAdapter(
            jpaMediaProcessingTaskRepository: com.only4.cap4k.reference.contentstudio.adapter.domain.repositories.MediaProcessingTaskRepository,
            entityManager: EntityManager,
        ): MediaProcessingTaskPersistenceAdapter =
            MediaProcessingTaskPersistenceAdapter(jpaMediaProcessingTaskRepository, entityManager)

        @Bean
        fun createContentDraftHandler(
            contentRepository: ContentRepository,
        ): CreateContentDraftCmd.Handler =
            CreateContentDraftCmd.Handler(contentRepository)

        @Bean
        fun submitContentForReviewHandler(
            contentRepository: ContentRepository,
        ): SubmitContentForReviewCmd.Handler =
            SubmitContentForReviewCmd.Handler(contentRepository)

        @Bean
        fun approveContentReviewHandler(
            contentRepository: ContentRepository,
        ): ApproveContentReviewCmd.Handler =
            ApproveContentReviewCmd.Handler(contentRepository)

        @Bean
        fun startMediaProcessingHandler(
            contentRepository: ContentRepository,
            mediaProcessingTaskRepository: MediaProcessingTaskRepository,
            mediaProcessingCli: MediaProcessingCli,
        ): StartMediaProcessingCmd.Handler =
            StartMediaProcessingCmd.Handler(contentRepository, mediaProcessingTaskRepository, mediaProcessingCli)

        @Bean
        fun markMediaProcessingSucceededHandler(
            mediaProcessingTaskRepository: MediaProcessingTaskRepository,
        ): MarkMediaProcessingSucceededCmd.Handler =
            MarkMediaProcessingSucceededCmd.Handler(mediaProcessingTaskRepository)

        @Bean
        fun publishContentHandler(
            contentRepository: ContentRepository,
            mediaProcessingTaskRepository: MediaProcessingTaskRepository,
            publicationEligibilityDomainService: PublicationEligibilityDomainService,
        ): PublishContentCmd.Handler =
            PublishContentCmd.Handler(
                contentRepository = contentRepository,
                mediaProcessingTaskRepository = mediaProcessingTaskRepository,
                publicationEligibilityDomainService = publicationEligibilityDomainService,
            )
    }

    @TestConfiguration(proxyBeanMethods = false)
    class TransactionProbeConfiguration {
        @Bean
        fun saveTransactionProbe(): SaveTransactionProbe = SaveTransactionProbe()

        @Bean
        @Primary
        fun probingContentRepository(
            delegate: ContentPersistenceAdapter,
            saveTransactionProbe: SaveTransactionProbe,
        ): ContentRepository =
            object : ContentRepository {
                override fun findById(id: UUID): Content? = delegate.findById(id)

                override fun save(content: Content): Content {
                    saveTransactionProbe.record("content", content.id.toString())
                    return delegate.save(content)
                }
            }

        @Bean
        @Primary
        fun probingMediaProcessingTaskRepository(
            delegate: MediaProcessingTaskPersistenceAdapter,
            saveTransactionProbe: SaveTransactionProbe,
        ): MediaProcessingTaskRepository =
            object : MediaProcessingTaskRepository {
                override fun findByContentId(contentId: UUID): MediaProcessingTask? =
                    delegate.findByContentId(contentId)

                override fun findByExternalTaskId(externalTaskId: String): MediaProcessingTask? =
                    delegate.findByExternalTaskId(externalTaskId)

                override fun findSubmittedTasks(): List<MediaProcessingTask> =
                    delegate.findSubmittedTasks()

                override fun save(task: MediaProcessingTask): MediaProcessingTask {
                    saveTransactionProbe.record("mediaProcessingTask", task.contentId.toString())
                    return delegate.save(task)
                }
            }
    }
}
