package com.only4.cap4k.reference.contentstudio.adapter

import com.only4.cap4k.ddd.core.application.query.Query
import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.reference.contentstudio.adapter.domain.repositories.ContentRepository as JpaContentRepository
import com.only4.cap4k.reference.contentstudio.adapter.domain.repositories.MediaProcessingTaskRepository as JpaMediaProcessingTaskRepository
import com.only4.cap4k.reference.contentstudio.adapter.application.queries.GetContentDetailQryHandler
import com.only4.cap4k.reference.contentstudio.adapter.application.queries.GetCurrentProcessingStatusQryHandler
import com.only4.cap4k.reference.contentstudio.adapter.persistence.ContentPersistenceAdapter
import com.only4.cap4k.reference.contentstudio.adapter.persistence.MediaProcessingTaskPersistenceAdapter
import com.only4.cap4k.reference.contentstudio.application.queries.content.read.GetContentDetailQry
import com.only4.cap4k.reference.contentstudio.application.queries.content.read.GetMediaProcessingStatusQry
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringJUnitConfig(QueryHandlerTest.TestConfig::class)
class QueryHandlerTest {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var contentJpaRepository: JpaContentRepository

    @Autowired
    private lateinit var mediaProcessingTaskJpaRepository: JpaMediaProcessingTaskRepository

    @Test
    fun `content detail query returns title body review status and content status`() {
        val contentId = UUID.randomUUID()
        contentJpaRepository.save(
            Content(
                id = contentId,
                title = "Repository-backed content",
                body = "Adapter query proof",
                mediaSourceKey = "media/repository-backed.mp4",
                reviewStatus = "APPROVED",
                contentStatus = "PUBLISHED",
                reviewerId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                reviewedAt = LocalDateTime.of(2026, 5, 9, 10, 30),
                publishedAt = LocalDateTime.of(2026, 5, 9, 11, 45),
                dbCreatedAt = LocalDateTime.of(2026, 5, 9, 9, 0),
                dbUpdatedAt = LocalDateTime.of(2026, 5, 9, 11, 45),
            )
        )

        val handler = queryBean<GetContentDetailQry.Request, GetContentDetailQry.Response>("getContentDetailQryHandler")

        val response = handler.exec(GetContentDetailQry.Request(contentId = contentId))

        assertEquals(contentId, response.contentId)
        assertEquals("Repository-backed content", response.title)
        assertEquals("Adapter query proof", response.body)
        assertEquals("media/repository-backed.mp4", response.mediaSourceKey)
        assertEquals("APPROVED", response.reviewStatus)
        assertEquals("PUBLISHED", response.contentStatus)
        assertNotNull(response.reviewerId)
        assertEquals(LocalDateTime.of(2026, 5, 9, 10, 30), response.reviewedAt)
        assertEquals(LocalDateTime.of(2026, 5, 9, 11, 45), response.publishedAt)
    }

    @Test
    fun `processing status query returns current task status and external task id`() {
        val contentId = UUID.randomUUID()
        contentJpaRepository.save(
            Content(
                id = contentId,
                title = "Media status content",
                body = "Adapter query proof",
                mediaSourceKey = "media/processing.mp4",
                reviewStatus = "APPROVED",
                contentStatus = "READY",
                reviewerId = null,
                reviewedAt = null,
                publishedAt = null,
                dbCreatedAt = LocalDateTime.of(2026, 5, 9, 9, 0),
                dbUpdatedAt = LocalDateTime.of(2026, 5, 9, 9, 0),
            )
        )
        mediaProcessingTaskJpaRepository.save(
            MediaProcessingTask(
                id = UUID.randomUUID(),
                contentId = contentId,
                externalTaskId = "ext-query-42",
                processingStatus = "SUBMITTED",
                dbCreatedAt = LocalDateTime.of(2026, 5, 9, 9, 5),
                dbUpdatedAt = LocalDateTime.of(2026, 5, 9, 9, 6),
            )
        )

        val handler =
            queryBean<GetMediaProcessingStatusQry.Request, GetMediaProcessingStatusQry.Response>(
                "getCurrentProcessingStatusQryHandler"
            )

        val response = handler.exec(GetMediaProcessingStatusQry.Request(contentId = contentId))

        assertEquals(contentId, response.contentId)
        assertNotNull(response.task)
        assertEquals("ext-query-42", response.task?.externalTaskId)
        assertEquals("SUBMITTED", response.task?.processingStatus)
    }

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(
        basePackageClasses = [
            JpaContentRepository::class,
            JpaMediaProcessingTaskRepository::class,
        ]
    )
    @ComponentScan(
        basePackageClasses = [
            GetContentDetailQryHandler::class,
            GetCurrentProcessingStatusQryHandler::class,
            ContentPersistenceAdapter::class,
            MediaProcessingTaskPersistenceAdapter::class,
        ]
    )
    internal class TestConfig {

        @Bean
        fun dataSource(): DataSource =
            EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build()

        @Bean
        fun entityManagerFactory(dataSource: DataSource): LocalContainerEntityManagerFactoryBean =
            LocalContainerEntityManagerFactoryBean().apply {
                setDataSource(dataSource)
                setPackagesToScan("com.only4.cap4k.reference.contentstudio.domain.aggregates")
                jpaVendorAdapter = HibernateJpaVendorAdapter()
                setJpaPropertyMap(
                    mapOf(
                        "hibernate.hbm2ddl.auto" to "create-drop",
                        "hibernate.dialect" to "org.hibernate.dialect.H2Dialect",
                    )
                )
            }

        @Bean
        fun transactionManager(entityManagerFactory: jakarta.persistence.EntityManagerFactory): PlatformTransactionManager =
            JpaTransactionManager(entityManagerFactory)

        @Bean
        fun persistenceExceptionTranslationPostProcessor(): PersistenceExceptionTranslationPostProcessor =
            PersistenceExceptionTranslationPostProcessor()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <REQUEST : RequestParam<RESPONSE>, RESPONSE : Any> queryBean(name: String): Query<REQUEST, RESPONSE> =
        applicationContext.getBean(name, Query::class.java) as Query<REQUEST, RESPONSE>
}
