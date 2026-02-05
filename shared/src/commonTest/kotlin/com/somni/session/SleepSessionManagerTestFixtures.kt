package com.somni.session

import com.somni.domain.model.SleepSession
import com.somni.domain.model.SyncStatus
import com.somni.domain.repository.SleepRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

object SleepSessionManagerTestFixtures {

    val fixedInstant: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000L)

    fun createManager(
        initialSessions: List<SleepSession> = emptyList(),
        timeProvider: com.somni.calculator.TimeProvider = object : com.somni.calculator.TimeProvider {
            override fun now(): Instant = fixedInstant
        },
        sessionIdGenerator: SessionIdGenerator = object : SessionIdGenerator {
            private var counter = 0
            override fun generate(): String = "session-test-${++counter}"
        },
        repositoryNowProvider: (() -> Instant)? = null,
    ): SleepSessionManager {
        val repo = when (repositoryNowProvider) {
            null -> InMemorySleepRepository(initialSessions)
            else -> InMemorySleepRepository(initialSessions, repositoryNowProvider)
        }
        return SleepSessionManager(repo, timeProvider, sessionIdGenerator)
    }

    fun createManagerWithSequentialTime(
        initialSessions: List<SleepSession> = emptyList(),
        startInstant: Instant = fixedInstant,
        sessionIdGenerator: SessionIdGenerator = object : SessionIdGenerator {
            private var counter = 0
            override fun generate(): String = "session-test-${++counter}"
        },
    ): Pair<SleepSessionManager, MutableList<Instant>> {
        val timeSequence = mutableListOf(startInstant)
        val timeProvider = object : com.somni.calculator.TimeProvider {
            override fun now(): Instant = timeSequence.last()
        }
        val repo = InMemorySleepRepository(initialSessions)
        val manager = SleepSessionManager(repo, timeProvider, sessionIdGenerator)
        return Pair(manager, timeSequence)
    }

    fun session(
        sessionId: String = "s-1",
        babyId: String = "baby1",
        startTime: Instant = fixedInstant,
        endTime: Instant? = null,
        durationMinutes: Int? = null,
    ): SleepSession {
        val duration = durationMinutes ?: endTime?.let { (it - startTime).inWholeMinutes.toInt() }
        return SleepSession(
        sessionId = sessionId,
        babyId = babyId,
        startTime = startTime,
        endTime = endTime,
        durationMinutes = duration,
        qualityScore = null,
        timezoneOffset = 0,
        syncStatus = SyncStatus.PENDING,
        initiatorDeviceId = "device1",
        modifiedAt = startTime,
        createdAt = startTime,
    )
    }
}

class InMemorySleepRepository(
    initialSessions: List<SleepSession> = emptyList(),
    private val nowProvider: () -> Instant = { Clock.System.now() },
) : SleepRepository {

    private val mutex = Mutex()
    private val sessions = initialSessions.associateBy { it.sessionId }.toMutableMap()

    override suspend fun getSessions(babyId: String, days: Int): List<SleepSession> = mutex.withLock {
        if (days <= 0) return@withLock emptyList()
        val cutoffMs = nowProvider().toEpochMilliseconds() - days * 24L * 60 * 60 * 1000
        val cutoff = Instant.fromEpochMilliseconds(cutoffMs)
        sessions.values
            .filter { it.babyId == babyId && it.startTime >= cutoff }
            .sortedByDescending { it.startTime }
    }

    override suspend fun getSession(sessionId: String): SleepSession? = mutex.withLock {
        sessions[sessionId]
    }

    override suspend fun getActiveSession(babyId: String): SleepSession? = mutex.withLock {
        sessions.values.find { it.babyId == babyId && it.endTime == null }
    }

    override suspend fun insertSession(session: SleepSession) = mutex.withLock {
        sessions[session.sessionId] = session
    }

    override suspend fun updateSession(session: SleepSession) = mutex.withLock {
        sessions[session.sessionId] = session
    }

    override suspend fun deleteSession(sessionId: String) {
        mutex.withLock { sessions.remove(sessionId) }
    }

    override suspend fun getPendingSyncSessions(): List<SleepSession> = mutex.withLock {
        sessions.values.filter { it.syncStatus == SyncStatus.PENDING }
    }

    override suspend fun markAsSynced(sessionId: String, syncedAt: Instant) {
        mutex.withLock {
            sessions[sessionId]?.let { s ->
                sessions[sessionId] = s.copy(syncStatus = SyncStatus.SYNCED)
            }
        }
    }
}
