package com.somni.calculator

import com.somni.domain.model.SleepSession
import com.somni.domain.model.SyncStatus
import com.somni.domain.model.WakeWindowRecommendation
import com.somni.domain.repository.SleepRepository
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

object SleepCalculatorTestFixtures {

    val fixedInstant: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000L)

    const val WAKE_WINDOW_BOUND_MIN = 0.70
    const val WAKE_WINDOW_BOUND_MAX = 1.30

    fun createCalculator(
        sessions: List<SleepSession> = emptyList(),
        timeProvider: TimeProvider = object : TimeProvider {
            override fun now(): Instant = fixedInstant
        },
    ): SleepCalculator {
        val repo = object : SleepRepository {
            override suspend fun getSessions(babyId: String, days: Int): List<SleepSession> = sessions
            override suspend fun getSession(sessionId: String): SleepSession? = null
            override suspend fun getActiveSession(babyId: String): SleepSession? = null
            override suspend fun insertSession(session: SleepSession) {}
            override suspend fun updateSession(session: SleepSession) {}
            override suspend fun deleteSession(sessionId: String) {}
            override suspend fun getPendingSyncSessions(): List<SleepSession> = emptyList()
            override suspend fun markAsSynced(sessionId: String, syncedAt: Instant) {}
        }
        val noOpScheduler = object : NotificationScheduler {
            override suspend fun scheduleWakeWindowNotification(recommendation: WakeWindowRecommendation) {}
        }
        return SleepCalculator(repo, noOpScheduler, timeProvider)
    }

    fun session(
        durationMinutes: Int,
        endTime: Instant = fixedInstant,
        timezoneOffset: Int = 0,
        sessionId: String = "s-$durationMinutes",
    ): SleepSession = SleepSession(
        sessionId = sessionId,
        babyId = "baby1",
        startTime = endTime.minus(durationMinutes.minutes),
        endTime = endTime,
        durationMinutes = durationMinutes,
        qualityScore = null,
        timezoneOffset = timezoneOffset,
        syncStatus = SyncStatus.SYNCED,
        initiatorDeviceId = "device1",
        modifiedAt = fixedInstant,
        createdAt = fixedInstant,
    )
}
