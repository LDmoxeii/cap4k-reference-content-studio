package com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.values

import com.only4.cap4k.ddd.core.domain.aggregate.annotation.Aggregate
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.UUID

@Aggregate(
    aggregate = "MediaProcessingTask",
    name = "MediaProcessingResultSnapshot",
    type = Aggregate.TYPE_VALUE_OBJECT,
    description = "Media processing result snapshot"
)
data class MediaProcessingResultSnapshot(
    val mediaProcessingTaskId: UUID,
    val externalTaskId: String,
    val resultStatus: MediaProcessingResultStatus,
    val assetSha256: String,
    val assetLocation: String,
    val completedAt: LocalDateTime,
    val dbCreatedAt: LocalDateTime,
    val dbUpdatedAt: LocalDateTime,
    val id: String = computeHash(
        mediaProcessingTaskId = mediaProcessingTaskId,
        externalTaskId = externalTaskId,
        resultStatus = resultStatus,
        assetSha256 = assetSha256,
        assetLocation = assetLocation,
    ),
) {
    init {
        require(externalTaskId.isNotBlank()) { "External task id must not be blank." }
        require(assetSha256.matches(assetSha256Pattern)) {
            "Asset SHA-256 must be a 64-character hexadecimal value."
        }
        require(assetLocation.isNotBlank()) { "Asset location must not be blank." }
    }

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
