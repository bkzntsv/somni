package com.somni.session

import com.somni.calculator.TimeProvider
import com.somni.domain.model.SleepSession
import com.somni.domain.model.SyncStatus
import com.somni.domain.repository.SleepRepository
import kotlinx.datetime.Instant

class SleepSessionManager(
    private val sleepRepository: SleepRepository,
    private val timeProvider: TimeProvider,
    private val sessionIdGenerator: SessionIdGenerator = DefaultSessionIdGenerator(timeProvider),
) {

    suspend fun startSleepSession(
        babyId: String,
        initiatorDeviceId: String,
        timezoneOffsetMinutes: Int = 0,
    ): SleepSession {
        val existing = sleepRepository.getActiveSession(babyId)
        if (existing != null) {
            throw IllegalStateException(
                "Baby $babyId already has an active session (${existing.sessionId}). End it before starting a new one."
            )
        }

        val now = timeProvider.now()
        val session = SleepSession(
            sessionId = sessionIdGenerator.generate(),
            babyId = babyId,
            startTime = now,
            endTime = null,
            durationMinutes = null,
            qualityScore = null,
            timezoneOffset = timezoneOffsetMinutes,
            syncStatus = SyncStatus.PENDING,
            initiatorDeviceId = initiatorDeviceId,
            modifiedAt = now,
            createdAt = now,
        )

        sleepRepository.insertSession(session)
        return session
    }

    suspend fun endSleepSession(sessionId: String): SleepSession {
        val existing = sleepRepository.getSession(sessionId)
            ?: throw IllegalArgumentException("Session $sessionId not found")
        require(existing.endTime == null) {
            "Session $sessionId is already completed"
        }

        val now = timeProvider.now()
        val durationMinutes = calculateDurationMinutes(existing.startTime, now)
        val updated = existing.copy(
            endTime = now,
            durationMinutes = durationMinutes,
            modifiedAt = now,
        )

        sleepRepository.updateSession(updated)
        return updated
    }

    suspend fun updateSession(
        sessionId: String,
        startTime: Instant?,
        endTime: Instant?,
    ): SleepSession {
        val existing = sleepRepository.getSession(sessionId)
            ?: throw IllegalArgumentException("Session $sessionId not found")

        val newStartTime = startTime ?: existing.startTime
        val newEndTime = when {
            endTime != null -> endTime
            existing.endTime != null -> existing.endTime
            else -> null
        }

        require(newEndTime == null || newEndTime >= newStartTime) {
            "endTime ($newEndTime) must be >= startTime ($newStartTime)"
        }

        val durationMinutes = newEndTime?.let { calculateDurationMinutes(newStartTime, it) }
        val now = timeProvider.now()
        val updated = existing.copy(
            startTime = newStartTime,
            endTime = newEndTime,
            durationMinutes = durationMinutes,
            modifiedAt = now,
        )

        sleepRepository.updateSession(updated)
        return updated
    }

    suspend fun getActiveSession(babyId: String): SleepSession? =
        sleepRepository.getActiveSession(babyId)

    suspend fun getSleepHistory(babyId: String, days: Int): List<SleepSession> =
        sleepRepository.getSessions(babyId, days)

    private fun calculateDurationMinutes(start: Instant, end: Instant): Int {
        val duration = end - start
        return duration.inWholeMinutes.toInt().coerceAtLeast(0)
    }
}

interface SessionIdGenerator {
    fun generate(): String
}

class DefaultSessionIdGenerator(
    private val timeProvider: TimeProvider,
) : SessionIdGenerator {
    override fun generate(): String {
        val ms = timeProvider.now().toEpochMilliseconds()
        val randomPart = (0..0xFFFFFF).random().toString(16).padStart(6, '0')
        return "session-$ms-$randomPart"
    }
}
