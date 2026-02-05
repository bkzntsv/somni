package com.somni.storage

import com.somni.database.SomniDatabase
import com.somni.domain.model.BabyProfile
import com.somni.domain.model.SleepSession
import com.somni.domain.model.SyncStatus
import com.somni.domain.repository.BabyProfileRepository
import com.somni.domain.repository.SleepRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalStorage(
    private val database: SomniDatabase,
) : SleepRepository, BabyProfileRepository {

    companion object {
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
    }

    private val sleepQueries = database.sleepSessionQueries
    private val babyQueries = database.babyProfileQueries
    private val settingsQueries = database.userSettingsQueries
    private val dbDispatcher = Dispatchers.Default

    override suspend fun getSessions(babyId: String, days: Int): List<SleepSession> = withContext(dbDispatcher) {
        val cutoffMs = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - days.toLong() * MILLIS_PER_DAY
        sleepQueries.selectRecentByBabyId(babyId, cutoffMs).executeAsList().map(::toSleepSession)
    }

    override suspend fun getSession(sessionId: String): SleepSession? = withContext(dbDispatcher) {
        sleepQueries.selectById(sessionId).executeAsOneOrNull()?.let(::toSleepSession)
    }

    override suspend fun getActiveSession(babyId: String): SleepSession? = withContext(dbDispatcher) {
        sleepQueries.selectByBabyId(babyId).executeAsList()
            .map(::toSleepSession)
            .firstOrNull { it.endTime == null }
    }

    override suspend fun insertSession(session: SleepSession) = withContext(dbDispatcher) {
        sleepQueries.insertSession(
            session_id = session.sessionId,
            baby_id = session.babyId,
            start_time = session.startTime.toEpochMilliseconds(),
            end_time = session.endTime?.toEpochMilliseconds(),
            duration_minutes = session.durationMinutes?.toLong(),
            quality_score = session.qualityScore?.toDouble(),
            timezone_offset = session.timezoneOffset.toLong(),
            sync_status = session.syncStatus.name,
            initiator_device_id = session.initiatorDeviceId,
            modified_at = session.modifiedAt.toEpochMilliseconds(),
            created_at = session.createdAt.toEpochMilliseconds(),
        )
        Unit
    }

    override suspend fun updateSession(session: SleepSession) = withContext(dbDispatcher) {
        val existing = sleepQueries.selectById(session.sessionId).executeAsOneOrNull() ?: return@withContext
        if (existing.end_time == null && session.endTime != null) {
            sleepQueries.updateSession(
                end_time = session.endTime.toEpochMilliseconds(),
                duration_minutes = session.durationMinutes?.toLong(),
                quality_score = session.qualityScore?.toDouble(),
                modified_at = session.modifiedAt.toEpochMilliseconds(),
                session_id = session.sessionId,
            )
        } else {
            sleepQueries.deleteSession(session.sessionId)
            insertSession(session)
        }
    }

    override suspend fun deleteSession(sessionId: String) = withContext(dbDispatcher) {
        sleepQueries.deleteSession(sessionId)
        Unit
    }

    override suspend fun getPendingSyncSessions(): List<SleepSession> = withContext(dbDispatcher) {
        sleepQueries.selectPendingSync().executeAsList().map(::toSleepSession)
    }

    override suspend fun markAsSynced(sessionId: String, syncedAt: Instant) = withContext(dbDispatcher) {
        sleepQueries.updateSyncStatus(
            sync_status = SyncStatus.SYNCED.name,
            modified_at = syncedAt.toEpochMilliseconds(),
            session_id = sessionId,
        )
        Unit
    }

    override suspend fun getAllProfiles(): List<BabyProfile> = withContext(dbDispatcher) {
        babyQueries.selectAllProfiles().executeAsList().map(::toBabyProfile)
    }

    override suspend fun getProfile(babyId: String): BabyProfile? = withContext(dbDispatcher) {
        babyQueries.selectProfileById(babyId).executeAsOneOrNull()?.let(::toBabyProfile)
    }

    override suspend fun getActiveProfile(userId: String): BabyProfile? = withContext(dbDispatcher) {
        babyQueries.selectActiveProfile(userId).executeAsOneOrNull()?.let(::toBabyProfile)
    }

    override suspend fun insertProfile(profile: BabyProfile) = withContext(dbDispatcher) {
        babyQueries.insertProfile(
            baby_id = profile.babyId,
            name = profile.name,
            birthdate = profile.birthdate.toEpochDays().toLong(),
            created_at = profile.createdAt.toEpochMilliseconds(),
        )
        Unit
    }

    override suspend fun updateProfile(profile: BabyProfile) = withContext(dbDispatcher) {
        babyQueries.updateProfile(
            name = profile.name,
            birthdate = profile.birthdate.toEpochDays().toLong(),
            baby_id = profile.babyId,
        )
        Unit
    }

    override suspend fun deleteProfile(babyId: String) = withContext(dbDispatcher) {
        babyQueries.deleteActiveByBabyId(babyId)
        babyQueries.deleteProfile(babyId)
        Unit
    }

    override suspend fun setActiveProfile(userId: String, babyId: String) = withContext(dbDispatcher) {
        babyQueries.setActiveProfile(user_id = userId, active_baby_id = babyId)
        Unit
    }

    suspend fun saveSetting(key: String, value: String) = withContext(dbDispatcher) {
        settingsQueries.insertOrUpdate(key, value)
        Unit
    }

    suspend fun getSetting(key: String): String? = withContext(dbDispatcher) {
        settingsQueries.selectByKey(key).executeAsOneOrNull()
    }

    suspend fun exportAllData(): String = withContext(dbDispatcher) {
        val sessions = sleepQueries.selectAll().executeAsList().map(::toSleepSession)
        val profiles = babyQueries.selectAllProfiles().executeAsList().map(::toBabyProfile)
        val settings = settingsQueries.selectAll().executeAsList()
            .associate { it.key to it.value_ }

        val export = GdprExport(
            exportedAt = kotlinx.datetime.Clock.System.now().toString(),
            sleepSessions = sessions.map(::SleepSessionExport),
            babyProfiles = profiles.map(::BabyProfileExport),
            userSettings = settings,
        )
        Json.encodeToString(export)
    }

    suspend fun deleteAllData() = withContext(dbDispatcher) {
        babyQueries.deleteAllActiveProfiles()
        sleepQueries.deleteAll()
        settingsQueries.deleteAll()
        babyQueries.deleteAllProfiles()
        Unit
    }

    private fun toSleepSession(row: com.somni.database.Sleep_sessions): SleepSession = SleepSession(
        sessionId = row.session_id,
        babyId = row.baby_id,
        startTime = Instant.fromEpochMilliseconds(row.start_time),
        endTime = row.end_time?.let(Instant::fromEpochMilliseconds),
        durationMinutes = row.duration_minutes?.toInt(),
        qualityScore = row.quality_score?.toFloat(),
        timezoneOffset = row.timezone_offset.toInt(),
        syncStatus = runCatching { SyncStatus.valueOf(row.sync_status) }.getOrDefault(SyncStatus.PENDING),
        initiatorDeviceId = row.initiator_device_id,
        modifiedAt = Instant.fromEpochMilliseconds(row.modified_at),
        createdAt = Instant.fromEpochMilliseconds(row.created_at),
    )

    private fun toBabyProfile(row: com.somni.database.Baby_profiles): BabyProfile = BabyProfile(
        babyId = row.baby_id,
        name = row.name,
        birthdate = LocalDate.fromEpochDays(row.birthdate.toInt()),
        createdAt = Instant.fromEpochMilliseconds(row.created_at),
    )

    private fun SleepSessionExport(s: SleepSession) = GdprSleepSession(
        sessionId = s.sessionId,
        babyId = s.babyId,
        startTime = s.startTime.toString(),
        endTime = s.endTime?.toString(),
        durationMinutes = s.durationMinutes,
        qualityScore = s.qualityScore,
        timezoneOffset = s.timezoneOffset,
        syncStatus = s.syncStatus.name,
        initiatorDeviceId = s.initiatorDeviceId,
        modifiedAt = s.modifiedAt.toString(),
        createdAt = s.createdAt.toString(),
    )

    private fun BabyProfileExport(p: BabyProfile) = GdprBabyProfile(
        babyId = p.babyId,
        name = p.name,
        birthdate = p.birthdate.toString(),
        createdAt = p.createdAt.toString(),
    )
}

@Serializable
data class GdprExport(
    val exportedAt: String,
    val sleepSessions: List<GdprSleepSession>,
    val babyProfiles: List<GdprBabyProfile>,
    val userSettings: Map<String, String>,
)

@Serializable
data class GdprSleepSession(
    val sessionId: String,
    val babyId: String,
    val startTime: String,
    val endTime: String?,
    val durationMinutes: Int?,
    val qualityScore: Float?,
    val timezoneOffset: Int,
    val syncStatus: String,
    val initiatorDeviceId: String,
    val modifiedAt: String,
    val createdAt: String,
)

@Serializable
data class GdprBabyProfile(
    val babyId: String,
    val name: String,
    val birthdate: String,
    val createdAt: String,
)
