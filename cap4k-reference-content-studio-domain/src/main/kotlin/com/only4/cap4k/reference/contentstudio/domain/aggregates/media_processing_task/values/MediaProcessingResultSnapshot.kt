package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values

import com.only4.cap4k.ddd.core.domain.aggregate.ValueObject
import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "media_processing_result_snapshot")
@Aggregate(
    aggregate = "MediaProcessingTask",
    name = "MediaProcessingResultSnapshot",
    type = Aggregate.TYPE_VALUE_OBJECT,
    description = "Media processing result snapshot"
)
class MediaProcessingResultSnapshot(
    mediaProcessingTaskId: UUID,
    externalTaskId: String,
    resultStatus: MediaProcessingResultStatus,
    assetSha256: String,
    assetLocation: String,
    completedAt: LocalDateTime,
    dbCreatedAt: LocalDateTime,
    dbUpdatedAt: LocalDateTime,
    id: String = computeHash(
        mediaProcessingTaskId = mediaProcessingTaskId,
        externalTaskId = externalTaskId,
        resultStatus = resultStatus,
        assetSha256 = assetSha256,
        assetLocation = assetLocation,
    ),
) : ValueObject<String> {

    @Id
    @Column(name = "id")
    var id: String = id
        internal set

    @Column(name = "media_processing_task_id")
    var mediaProcessingTaskId: UUID = mediaProcessingTaskId
        internal set

    @Column(name = "external_task_id")
    var externalTaskId: String = externalTaskId
        internal set

    @Column(name = "result_status")
    @Convert(converter = MediaProcessingResultStatus.Converter::class)
    var resultStatus: MediaProcessingResultStatus = resultStatus
        internal set

    @Column(name = "asset_sha256")
    var assetSha256: String = assetSha256
        internal set

    @Column(name = "asset_location")
    var assetLocation: String = assetLocation
        internal set

    @Column(name = "completed_at")
    var completedAt: LocalDateTime = completedAt
        internal set

    @Column(name = "db_created_at")
    var dbCreatedAt: LocalDateTime = dbCreatedAt
        internal set

    @Column(name = "db_updated_at")
    var dbUpdatedAt: LocalDateTime = dbUpdatedAt
        internal set

    override fun hash(): String = id

    companion object {
        private val assetSha256Pattern = Regex("[a-fA-F0-9]{64}")

        fun create(
            mediaProcessingTaskId: UUID,
            externalTaskId: String,
            resultStatus: MediaProcessingResultStatus,
            assetSha256: String,
            assetLocation: String,
            completedAt: LocalDateTime,
            now: LocalDateTime,
        ): MediaProcessingResultSnapshot {
            require(externalTaskId.isNotBlank()) { "External task id must not be blank." }
            require(assetSha256.matches(assetSha256Pattern)) {
                "Asset SHA-256 must be a 64-character hexadecimal value."
            }
            require(assetLocation.isNotBlank()) { "Asset location must not be blank." }

            return MediaProcessingResultSnapshot(
                mediaProcessingTaskId = mediaProcessingTaskId,
                externalTaskId = externalTaskId.trim(),
                resultStatus = resultStatus,
                assetSha256 = assetSha256.lowercase(),
                assetLocation = assetLocation.trim(),
                completedAt = completedAt,
                dbCreatedAt = now,
                dbUpdatedAt = now,
            )
        }

        fun computeHash(
            mediaProcessingTaskId: UUID,
            externalTaskId: String,
            resultStatus: MediaProcessingResultStatus,
            assetSha256: String,
            assetLocation: String,
        ): String {
            val raw = listOf(
                mediaProcessingTaskId.toString(),
                externalTaskId.trim(),
                resultStatus.name,
                assetSha256.lowercase(),
                assetLocation.trim(),
            ).joinToString("|")
            val bytes = MessageDigest.getInstance("MD5").digest(raw.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it.toInt() and 0xff) }
        }
    }
}
