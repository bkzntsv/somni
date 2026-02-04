package com.somni.domain.repository

import com.somni.domain.model.SleepSession
import kotlinx.datetime.Instant

interface SleepRepository {
    suspend fun getSessions(babyId: String, days: Int): List<SleepSession>
    suspend fun getSession(sessionId: String): SleepSession?
    suspend fun getActiveSession(babyId: String): SleepSession?
    suspend fun insertSession(session: SleepSession)
    suspend fun updateSession(session: SleepSession)
    suspend fun deleteSession(sessionId: String)
    suspend fun getPendingSyncSessions(): List<SleepSession>
    suspend fun markAsSynced(sessionId: String, syncedAt: Instant)
}
