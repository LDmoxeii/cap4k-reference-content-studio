package com.only4.cap4k.reference.contentstudio.domain.aggregates.content

import com.only4.cap4k.ddd.core.domain.id.ApplicationSideId
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "content")
class Content(
    id: UUID = UUID(0L, 0L),
    title: String,
    body: String,
    mediaSourceKey: String,
    reviewStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus,
    contentStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus,
    reviewerId: UUID? = null,
    reviewedAt: java.time.LocalDateTime? = null,
    publishedAt: java.time.LocalDateTime? = null,
    dbCreatedAt: java.time.LocalDateTime,
    dbUpdatedAt: java.time.LocalDateTime
) {

    @Id
    @field:ApplicationSideId(strategy = "uuid7")
    @Column(name = "id", insertable = true, updatable = false)
    var id: UUID = id
        internal set

    @Column(name = "title")
    var title: String = title
        internal set

    @Column(name = "body")
    var body: String = body
        internal set

    @Column(name = "media_source_key")
    var mediaSourceKey: String = mediaSourceKey
        internal set

    @Column(name = "review_status")
    @Convert(converter = com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus.Converter::class)
    var reviewStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus = reviewStatus
        internal set

    @Column(name = "content_status")
    @Convert(converter = com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus.Converter::class)
    var contentStatus: com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus = contentStatus
        internal set

    @Column(name = "reviewer_id")
    var reviewerId: UUID? = reviewerId
        internal set

    @Column(name = "reviewed_at")
    var reviewedAt: java.time.LocalDateTime? = reviewedAt
        internal set

    @Column(name = "published_at")
    var publishedAt: java.time.LocalDateTime? = publishedAt
        internal set

    @Column(name = "db_created_at")
    var dbCreatedAt: java.time.LocalDateTime = dbCreatedAt
        internal set

    @Column(name = "db_updated_at")
    var dbUpdatedAt: java.time.LocalDateTime = dbUpdatedAt
        internal set

}
