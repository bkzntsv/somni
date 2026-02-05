package com.somni.storage

import com.somni.database.SomniDatabase
import com.somni.database.createSqlDriver
import com.somni.domain.model.BabyProfile
import com.somni.domain.model.SleepSession
import com.somni.domain.model.SyncStatus
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocalStorageTest {

    private fun createStorage(): LocalStorage {
        val driver = createSqlDriver("test.db", null)
        val database = SomniDatabase(driver)
        return LocalStorage(database)
    }

    @Test
    fun `insert and get session preserves data integrity`() = runTest {
        val storage = createStorage()
        val session = SleepSession(
            sessionId = "session-1",
            babyId = "baby-1",
            startTime = Instant.fromEpochMilliseconds(1_700_000_000_000L),
            endTime = Instant.fromEpochMilliseconds(1_700_000_360_000L),
            durationMinutes = 6,
            qualityScore = 0.9f,
            timezoneOffset = 180,
            syncStatus = SyncStatus.PENDING,
            initiatorDeviceId = "device-1",
            modifiedAt = Instant.fromEpochMilliseconds(1_700_000_360_000L),
            createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
        )
        storage.insertSession(session)
        val read = storage.getSession(session.sessionId)
        assertNotNull(read)
        assertEquals(session.sessionId, read!!.sessionId)
        assertEquals(session.babyId, read.babyId)
        assertEquals(session.startTime, read.startTime)
        assertEquals(session.endTime, read.endTime)
        assertEquals(session.durationMinutes, read.durationMinutes)
        assertEquals(session.qualityScore, read.qualityScore)
        assertEquals(session.timezoneOffset, read.timezoneOffset)
        assertEquals(session.syncStatus, read.syncStatus)
        assertEquals(session.initiatorDeviceId, read.initiatorDeviceId)
        assertEquals(session.modifiedAt, read.modifiedAt)
        assertEquals(session.createdAt, read.createdAt)
    }

    @Test
    fun `updateSession and getSession preserves data`() = runTest {
        val storage = createStorage()
        val session = SleepSession(
            sessionId = "s2",
            babyId = "b1",
            startTime = Instant.fromEpochMilliseconds(1_700_000_000_000L),
            endTime = null,
            durationMinutes = null,
            qualityScore = null,
            timezoneOffset = 0,
            syncStatus = SyncStatus.PENDING,
            initiatorDeviceId = "d1",
            modifiedAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
            createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
        )
        storage.insertSession(session)
        val updated = session.copy(
            endTime = Instant.fromEpochMilliseconds(1_700_000_360_000L),
            durationMinutes = 6,
            modifiedAt = Instant.fromEpochMilliseconds(1_700_000_360_000L),
        )
        storage.updateSession(updated)
        val read = storage.getSession(session.sessionId)
        assertNotNull(read)
        assertEquals(updated.endTime, read!!.endTime)
        assertEquals(updated.durationMinutes, read.durationMinutes)
    }

    @Test
    fun `export produces valid JSON with all user data`() = runTest {
        val storage = createStorage()
        val session = SleepSession(
            sessionId = "export-s1",
            babyId = "export-b1",
            startTime = Instant.fromEpochMilliseconds(1_700_000_000_000L),
            endTime = Instant.fromEpochMilliseconds(1_700_000_360_000L),
            durationMinutes = 6,
            qualityScore = null,
            timezoneOffset = 0,
            syncStatus = SyncStatus.SYNCED,
            initiatorDeviceId = "d1",
            modifiedAt = Instant.fromEpochMilliseconds(1_700_000_360_000L),
            createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
        )
        val profile = BabyProfile(
            babyId = "export-b1",
            name = "Test Baby",
            birthdate = LocalDate(2025, 1, 1),
            createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
        )
        storage.insertSession(session)
        storage.insertProfile(profile)
        storage.saveSetting("key1", "value1")

        val json = storage.exportAllData()
        assertTrue(json.isNotBlank())
        val export = Json.decodeFromString<GdprExport>(json)
        assertEquals(1, export.sleepSessions.size)
        assertEquals("export-s1", export.sleepSessions.single().sessionId)
        assertEquals(1, export.babyProfiles.size)
        assertEquals("Test Baby", export.babyProfiles.single().name)
        assertTrue(export.userSettings.containsKey("key1"))
        assertEquals("value1", export.userSettings["key1"])
    }

    @Test
    fun `getPendingSyncSessions returns only PENDING`() = runTest {
        val storage = createStorage()
        val pending = SleepSession(
            sessionId = "p1",
            babyId = "b1",
            startTime = Instant.fromEpochMilliseconds(1_700_000_000_000L),
            endTime = null,
            durationMinutes = null,
            qualityScore = null,
            timezoneOffset = 0,
            syncStatus = SyncStatus.PENDING,
            initiatorDeviceId = "d1",
            modifiedAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
            createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
        )
        val synced = pending.copy(
            sessionId = "p2",
            syncStatus = SyncStatus.SYNCED,
        )
        storage.insertSession(pending)
        storage.insertSession(synced)
        val list = storage.getPendingSyncSessions()
        assertEquals(1, list.size)
        assertEquals("p1", list.single().sessionId)
    }

    @Test
    fun `markAsSynced updates status`() = runTest {
        val storage = createStorage()
        val session = SleepSession(
            sessionId = "m1",
            babyId = "b1",
            startTime = Instant.fromEpochMilliseconds(1_700_000_000_000L),
            endTime = Instant.fromEpochMilliseconds(1_700_000_360_000L),
            durationMinutes = 6,
            qualityScore = null,
            timezoneOffset = 0,
            syncStatus = SyncStatus.PENDING,
            initiatorDeviceId = "d1",
            modifiedAt = Instant.fromEpochMilliseconds(1_700_000_360_000L),
            createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
        )
        storage.insertSession(session)
        storage.markAsSynced("m1", Instant.fromEpochMilliseconds(1_700_000_400_000L))
        val read = storage.getSession("m1")
        assertNotNull(read)
        assertEquals(SyncStatus.SYNCED, read!!.syncStatus)
    }

    @Test
    fun `deleteAllData removes all data`() = runTest {
        val storage = createStorage()
        storage.insertSession(
            SleepSession(
                sessionId = "d1",
                babyId = "b1",
                startTime = Instant.fromEpochMilliseconds(1_700_000_000_000L),
                endTime = null,
                durationMinutes = null,
                qualityScore = null,
                timezoneOffset = 0,
                syncStatus = SyncStatus.PENDING,
                initiatorDeviceId = "d1",
                modifiedAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
                createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
            ),
        )
        storage.saveSetting("k", "v")
        storage.deleteAllData()
        assertNull(storage.getSession("d1"))
        assertNull(storage.getSetting("k"))
    }
}
