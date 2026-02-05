package com.somni.session

import com.somni.calculator.TimeProvider
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.milliseconds

class SleepSessionManagerTest {

    private val fixedInstant = SleepSessionManagerTestFixtures.fixedInstant

    @Test
    fun `startSleepSession creates session with millisecond-precision startTime`() = runTest {
        val manager = SleepSessionManagerTestFixtures.createManager()

        val session = manager.startSleepSession("baby1", "device1")

        assertEquals("baby1", session.babyId)
        assertEquals("device1", session.initiatorDeviceId)
        assertEquals(fixedInstant, session.startTime)
        assertEquals(fixedInstant.toEpochMilliseconds(), session.startTime.toEpochMilliseconds())
        assertNull(session.endTime)
        assertNull(session.durationMinutes)
    }

    @Test
    fun `startSleepSession throws when baby has active session`() = runTest {
        val activeSession = SleepSessionManagerTestFixtures.session(
            sessionId = "active-1",
            endTime = null,
            durationMinutes = null,
        )
        val manager = SleepSessionManagerTestFixtures.createManager(initialSessions = listOf(activeSession))

        val ex = assertFailsWith<IllegalStateException> {
            manager.startSleepSession("baby1", "device1")
        }
        assertTrue(ex.message?.contains("active-1") == true, "Message should include active session id: ${ex.message}")
    }

    @Test
    fun `endSleepSession sets endTime and calculates duration correctly`() = runTest {
        val endTime = fixedInstant.plus(90.minutes)
        val (manager, timeSequence) = SleepSessionManagerTestFixtures.createManagerWithSequentialTime()
        manager.startSleepSession("baby1", "device1")
        timeSequence.add(endTime)

        val completed = manager.endSleepSession("session-test-1")

        assertEquals(endTime, completed.endTime)
        assertEquals(90, completed.durationMinutes)
    }

    @Test
    fun `duration calculation matches endTime minus startTime`() = runTest {
        val startTime = fixedInstant
        val endTime = fixedInstant.plus(127.minutes)
        val (manager, timeSequence) = SleepSessionManagerTestFixtures.createManagerWithSequentialTime(startInstant = startTime)
        manager.startSleepSession("baby1", "device1")
        timeSequence.add(endTime)

        val completed = manager.endSleepSession("session-test-1")

        val expectedMinutes = (endTime - startTime).inWholeMinutes.toInt()
        assertEquals(expectedMinutes, completed.durationMinutes)
        assertEquals(127, completed.durationMinutes)
    }

    @Test
    fun `endSleepSession throws when session not found`() = runTest {
        val manager = SleepSessionManagerTestFixtures.createManager()

        assertFailsWith<IllegalArgumentException> {
            manager.endSleepSession("non-existent")
        }
    }

    @Test
    fun `endSleepSession throws when session already completed`() = runTest {
        val completedSession = SleepSessionManagerTestFixtures.session(
            sessionId = "s-completed",
            endTime = fixedInstant.plus(60.minutes),
            durationMinutes = 60,
        )
        val manager = SleepSessionManagerTestFixtures.createManager(initialSessions = listOf(completedSession))

        assertFailsWith<IllegalArgumentException> {
            manager.endSleepSession("s-completed")
        }
    }

    @Test
    fun `updateSession applies manual start time adjustment`() = runTest {
        val originalStart = fixedInstant
        val originalEnd = fixedInstant.plus(60.minutes)
        val session = SleepSessionManagerTestFixtures.session(
            sessionId = "s-edit",
            startTime = originalStart,
            endTime = originalEnd,
            durationMinutes = 60,
        )
        val manager = SleepSessionManagerTestFixtures.createManager(initialSessions = listOf(session))
        val newStart = originalStart.plus(15.minutes)

        val updated = manager.updateSession("s-edit", startTime = newStart, endTime = null)

        assertEquals(newStart, updated.startTime)
        assertEquals(originalEnd, updated.endTime)
        assertEquals(45, updated.durationMinutes)
    }

    @Test
    fun `updateSession applies manual end time adjustment`() = runTest {
        val originalStart = fixedInstant
        val originalEnd = fixedInstant.plus(60.minutes)
        val session = SleepSessionManagerTestFixtures.session(
            sessionId = "s-edit",
            startTime = originalStart,
            endTime = originalEnd,
            durationMinutes = 60,
        )
        val manager = SleepSessionManagerTestFixtures.createManager(initialSessions = listOf(session))
        val newEnd = originalStart.plus(90.minutes)

        val updated = manager.updateSession("s-edit", startTime = null, endTime = newEnd)

        assertEquals(originalStart, updated.startTime)
        assertEquals(newEnd, updated.endTime)
        assertEquals(90, updated.durationMinutes)
    }

    @Test
    fun `updateSession preserves data integrity - rejects endTime before startTime`() = runTest {
        val originalStart = fixedInstant
        val originalEnd = fixedInstant.plus(60.minutes)
        val session = SleepSessionManagerTestFixtures.session(
            sessionId = "s-edit",
            startTime = originalStart,
            endTime = originalEnd,
        )
        val manager = SleepSessionManagerTestFixtures.createManager(initialSessions = listOf(session))
        val invalidEnd = originalStart.minus(10.minutes)

        assertFailsWith<IllegalArgumentException> {
            manager.updateSession("s-edit", startTime = null, endTime = invalidEnd)
        }
    }

    @Test
    fun `updateSession throws when session not found`() = runTest {
        val manager = SleepSessionManagerTestFixtures.createManager()

        assertFailsWith<IllegalArgumentException> {
            manager.updateSession("non-existent", startTime = fixedInstant, endTime = null)
        }
    }

    @Test
    fun `timestamps recorded with millisecond precision`() = runTest {
        val ms = 1_700_000_000_123L
        val instantWithMs = Instant.fromEpochMilliseconds(ms)
        val timeProvider = object : TimeProvider {
            override fun now(): Instant = instantWithMs
        }
        val manager = SleepSessionManagerTestFixtures.createManager(timeProvider = timeProvider)

        val session = manager.startSleepSession("baby1", "device1")

        assertEquals(ms, session.startTime.toEpochMilliseconds())
        assertEquals(instantWithMs, session.startTime)
    }

    @Test
    fun `duration calculation for sub-minute session rounds down`() = runTest {
        val startTime = fixedInstant
        val endTime = fixedInstant.plus(45.minutes).plus(30_000.milliseconds)
        val (manager, timeSequence) = SleepSessionManagerTestFixtures.createManagerWithSequentialTime(startInstant = startTime)
        manager.startSleepSession("baby1", "device1")
        timeSequence.add(endTime)

        val completed = manager.endSleepSession("session-test-1")

        assertEquals(45, completed.durationMinutes)
    }

    @Test
    fun `getActiveSession returns active session`() = runTest {
        val manager = SleepSessionManagerTestFixtures.createManager()
        manager.startSleepSession("baby1", "device1")

        val active = manager.getActiveSession("baby1")

        assertEquals("session-test-1", active?.sessionId)
        assertNull(active?.endTime)
    }

    @Test
    fun `getActiveSession returns null when no active session`() = runTest {
        val manager = SleepSessionManagerTestFixtures.createManager()

        assertNull(manager.getActiveSession("baby1"))
    }

    @Test
    fun `getSleepHistory returns sessions for baby`() = runTest {
        val session = SleepSessionManagerTestFixtures.session(babyId = "baby1")
        val nowForFilter = fixedInstant.plus(15.days)
        val manager = SleepSessionManagerTestFixtures.createManager(
            initialSessions = listOf(session),
            repositoryNowProvider = { nowForFilter },
        )

        val history = manager.getSleepHistory("baby1", days = 30)

        assertEquals(1, history.size)
        assertEquals("baby1", history[0].babyId)
    }
}
