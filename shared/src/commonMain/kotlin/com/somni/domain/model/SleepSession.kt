package com.somni.domain.model

import kotlinx.datetime.Instant

data class SleepSession(
    val sessionId: String,
    val babyId: String,
    val startTime: Instant,
    val endTime: Instant?,
    val durationMinutes: Int?,
    val qualityScore: Float?,
    val timezoneOffset: Int,
    val syncStatus: SyncStatus,
    val initiatorDeviceId: String,
    val modifiedAt: Instant,
    val createdAt: Instant
)

enum class SyncStatus {
    PENDING,
    SYNCED,
    FAILED
}
