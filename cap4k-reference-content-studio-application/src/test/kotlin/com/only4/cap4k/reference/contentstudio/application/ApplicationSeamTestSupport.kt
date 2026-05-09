package com.only4.cap4k.reference.contentstudio.application

import com.only4.cap4k.ddd.core.application.RequestParam
import com.only4.cap4k.ddd.core.application.RequestRecord
import com.only4.cap4k.ddd.core.application.RequestSupervisor
import com.only4.cap4k.reference.contentstudio.application.ports.ContentRepository
import com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingCli
import com.only4.cap4k.reference.contentstudio.application.ports.MediaProcessingTaskRepository
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ContentStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.ReviewStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingStatus
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

internal class InMemoryContentRepository(
    initial: List<Content> = emptyList(),
) : ContentRepository {
    private val contents = initial.associateBy { it.id }.toMutableMap()
    val saveCalls = mutableListOf<UUID>()

    override fun findById(id: UUID): Content? = contents[id]

    override fun save(content: Content): Content {
        contents[content.id] = content
        saveCalls += content.id
        return content
    }

    fun require(id: UUID): Content = checkNotNull(findById(id))
}

internal class InMemoryMediaProcessingTaskRepository(
    initial: List<MediaProcessingTask> = emptyList(),
) : MediaProcessingTaskRepository {
    private val tasks = initial.associateBy { it.contentId }.toMutableMap()
    val saveCalls = mutableListOf<UUID>()

    override fun findByContentId(contentId: UUID): MediaProcessingTask? = tasks[contentId]

    override fun findByExternalTaskId(externalTaskId: String): MediaProcessingTask? =
        tasks.values.firstOrNull { it.externalTaskId == externalTaskId }

    override fun findSubmittedTasks(): List<MediaProcessingTask> =
        tasks.values.filter {
            it.processingStatus == MediaProcessingStatus.SUBMITTED.name && !it.externalTaskId.isNullOrBlank()
        }

    override fun save(task: MediaProcessingTask): MediaProcessingTask {
        tasks[task.contentId] = task
        saveCalls += task.contentId
        return task
    }

    fun require(contentId: UUID): MediaProcessingTask = checkNotNull(findByContentId(contentId))
}

internal class FakeMediaProcessingCli(
    private val accepted: Boolean = true,
    private val externalTaskId: String = "ext-123",
    private val polledStatuses: Map<String, MediaProcessingCli.StatusResponse> = emptyMap(),
) : MediaProcessingCli {
    data class Call(
        val contentId: UUID,
        val mediaSourceKey: String,
    )

    val calls = mutableListOf<Call>()
    val statusCalls = mutableListOf<String>()

    override fun start(contentId: UUID, mediaSourceKey: String): MediaProcessingCli.Response {
        calls += Call(contentId, mediaSourceKey)
        return MediaProcessingCli.Response(
            accepted = accepted,
            externalTaskId = externalTaskId,
        )
    }

    override fun getStatus(externalTaskId: String): MediaProcessingCli.StatusResponse {
        statusCalls += externalTaskId
        return polledStatuses[externalTaskId]
            ?: MediaProcessingCli.StatusResponse(MediaProcessingCli.ExternalTaskStatus.SUBMITTED)
    }
}

internal class RecordingRequestSupervisor : RequestSupervisor {
    private val handlers = mutableMapOf<Class<*>, (RequestParam<*>) -> Any>()
    val sentRequests = mutableListOf<RequestParam<*>>()

    fun <REQUEST : RequestParam<RESPONSE>, RESPONSE : Any> register(
        requestType: Class<REQUEST>,
        handler: (REQUEST) -> RESPONSE,
    ) {
        handlers[requestType] = { request -> handler(requestType.cast(request)) }
    }

    override fun <REQUEST : RequestParam<RESPONSE>, RESPONSE : Any> send(request: REQUEST): RESPONSE {
        sentRequests += request
        @Suppress("UNCHECKED_CAST")
        val handler = handlers[request::class.java] as? ((REQUEST) -> RESPONSE)
            ?: error("No handler registered for ${request::class.java.name}")
        return handler(request)
    }

    override fun <REQUEST : RequestParam<RESPONSE>, RESPONSE : Any> schedule(
        request: REQUEST,
        schedule: LocalDateTime,
    ): String = error("Scheduling is not used in these seam tests.")

    override fun <R : Any> result(requestId: String): R? = error("Results are not used in these seam tests.")
}

internal fun contentFixture(
    id: UUID = UUID.randomUUID(),
    mediaSourceKey: String = "media/demo.mp4",
    reviewStatus: ReviewStatus = ReviewStatus.PENDING,
    contentStatus: ContentStatus = ContentStatus.DRAFT,
    reviewerId: UUID? = null,
    reviewedAt: LocalDateTime? = null,
    publishedAt: LocalDateTime? = null,
): Content =
    Content(
        id = id,
        title = "How application seams work",
        body = "Reference content",
        mediaSourceKey = mediaSourceKey,
        reviewStatus = reviewStatus.name,
        contentStatus = contentStatus.name,
        reviewerId = reviewerId,
        reviewedAt = reviewedAt,
        publishedAt = publishedAt,
        dbCreatedAt = LocalDateTime.of(2026, 5, 9, 8, 0),
        dbUpdatedAt = LocalDateTime.of(2026, 5, 9, 8, 0),
    )

internal fun mediaProcessingTaskFixture(
    contentId: UUID,
    status: MediaProcessingStatus = MediaProcessingStatus.SUBMITTED,
    externalTaskId: String? = "ext-123",
): MediaProcessingTask =
    MediaProcessingTask(
        id = UUID.randomUUID(),
        contentId = contentId,
        externalTaskId = externalTaskId,
        processingStatus = status.name,
        dbCreatedAt = LocalDateTime.of(2026, 5, 9, 8, 5),
        dbUpdatedAt = LocalDateTime.of(2026, 5, 9, 8, 5),
    )
