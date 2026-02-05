package com.somni.calculator

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class SleepCalculatorTest {

    private val fixedInstant = SleepCalculatorTestFixtures.fixedInstant

    private fun createCalculator(
        sessions: List<com.somni.domain.model.SleepSession> = emptyList(),
        timeProvider: TimeProvider = object : TimeProvider {
            override fun now() = SleepCalculatorTestFixtures.fixedInstant
        },
    ) = SleepCalculatorTestFixtures.createCalculator(sessions, timeProvider)

    private fun session(
        durationMinutes: Int,
        endTime: kotlinx.datetime.Instant = fixedInstant,
        timezoneOffset: Int = 0,
    ) = SleepCalculatorTestFixtures.session(durationMinutes, endTime, timezoneOffset)

    @Test
    fun `computeBaselineWakeWindow returns 45 min for 0-4 weeks`() = runTest {
        val calculator = createCalculator()
        assertEquals(45.minutes, calculator.computeBaselineWakeWindow(0))
        assertEquals(45.minutes, calculator.computeBaselineWakeWindow(4))
    }

    @Test
    fun `computeBaselineWakeWindow throws for negative ageInWeeks`() = runTest {
        val calculator = createCalculator()
        assertFailsWith<IllegalArgumentException> {
            calculator.computeBaselineWakeWindow(-1)
        }
    }

    @Test
    fun `computeBaselineWakeWindow returns 60 min for 5-8 weeks`() = runTest {
        val calculator = createCalculator()
        assertEquals(60.minutes, calculator.computeBaselineWakeWindow(5))
        assertEquals(60.minutes, calculator.computeBaselineWakeWindow(8))
    }

    @Test
    fun `computeBaselineWakeWindow returns 75 min for 9-12 weeks`() = runTest {
        val calculator = createCalculator()
        assertEquals(75.minutes, calculator.computeBaselineWakeWindow(9))
        assertEquals(75.minutes, calculator.computeBaselineWakeWindow(12))
    }

    @Test
    fun `computeBaselineWakeWindow returns 90 min for 13-16 weeks`() = runTest {
        val calculator = createCalculator()
        assertEquals(90.minutes, calculator.computeBaselineWakeWindow(13))
        assertEquals(90.minutes, calculator.computeBaselineWakeWindow(16))
    }

    @Test
    fun `computeBaselineWakeWindow returns 120 min for 17-24 weeks`() = runTest {
        val calculator = createCalculator()
        assertEquals(120.minutes, calculator.computeBaselineWakeWindow(17))
        assertEquals(120.minutes, calculator.computeBaselineWakeWindow(24))
    }

    @Test
    fun `computeBaselineWakeWindow returns 150 min for 25-36 weeks`() = runTest {
        val calculator = createCalculator()
        assertEquals(150.minutes, calculator.computeBaselineWakeWindow(25))
        assertEquals(150.minutes, calculator.computeBaselineWakeWindow(36))
    }

    @Test
    fun `computeBaselineWakeWindow returns 180 min for 37-52 weeks`() = runTest {
        val calculator = createCalculator()
        assertEquals(180.minutes, calculator.computeBaselineWakeWindow(37))
        assertEquals(180.minutes, calculator.computeBaselineWakeWindow(52))
    }

    @Test
    fun `computeBaselineWakeWindow returns 240 min for 53+ weeks`() = runTest {
        val calculator = createCalculator()
        assertEquals(240.minutes, calculator.computeBaselineWakeWindow(53))
        assertEquals(240.minutes, calculator.computeBaselineWakeWindow(104))
    }

    @Test
    fun `computeAdaptiveWakeWindow returns baseline when no recent sessions`() = runTest {
        val calculator = createCalculator()
        val rec = calculator.computeAdaptiveWakeWindow("baby1", ageInWeeks = 12)
        assertEquals(75.minutes, rec.duration)
        assertEquals(75.minutes, rec.baselineWindow)
        assertNull(rec.adjustmentReason)
        assertTrue(rec.confidence > 0f)
        assertTrue(rec.nextSleepTime > fixedInstant)
    }

    @Test
    fun `computeAdaptiveWakeWindow throws for negative ageInWeeks`() = runTest {
        val calculator = createCalculator()
        assertFailsWith<IllegalArgumentException> {
            calculator.computeAdaptiveWakeWindow("baby1", ageInWeeks = -1)
        }
    }

    @Test
    fun `computeAdaptiveWakeWindow reduces window when recent sleep is poor`() = runTest {
        val sessions = listOf(
            session(50, endTime = fixedInstant.minus(70.minutes)),
            session(50, endTime = fixedInstant.minus(20.minutes)),
            session(20, endTime = fixedInstant),
        )
        val calculator = createCalculator(sessions = sessions)
        val rec = calculator.computeAdaptiveWakeWindow("baby1", ageInWeeks = 12)
        assertTrue(rec.duration < 75.minutes)
        assertTrue(rec.adjustmentReason != null)
        assertTrue(rec.duration >= 75.minutes * SleepCalculatorTestFixtures.WAKE_WINDOW_BOUND_MIN)
        assertTrue(rec.duration <= 75.minutes * SleepCalculatorTestFixtures.WAKE_WINDOW_BOUND_MAX)
    }

    @Test
    fun `computeAdaptiveWakeWindow increases window when recent sleep is excellent`() = runTest {
        val sessions = listOf(
            session(60, endTime = fixedInstant.minus(90.minutes)),
            session(60, endTime = fixedInstant.minus(30.minutes)),
            session(90, endTime = fixedInstant),
        )
        val calculator = createCalculator(sessions = sessions)
        val rec = calculator.computeAdaptiveWakeWindow("baby1", ageInWeeks = 12)
        assertTrue(rec.duration > 75.minutes)
        assertTrue(rec.adjustmentReason != null)
        assertTrue(rec.duration >= 75.minutes * SleepCalculatorTestFixtures.WAKE_WINDOW_BOUND_MIN)
        assertTrue(rec.duration <= 75.minutes * SleepCalculatorTestFixtures.WAKE_WINDOW_BOUND_MAX)
    }

    @Test
    fun `computeAdaptiveWakeWindow keeps result within 30 percent of baseline`() = runTest {
        val sessions = listOf(session(10))
        val calculator = createCalculator(sessions = sessions)
        val rec = calculator.computeAdaptiveWakeWindow("baby1", ageInWeeks = 12)
        val baseline = 75.minutes
        assertTrue(rec.duration >= baseline * SleepCalculatorTestFixtures.WAKE_WINDOW_BOUND_MIN)
        assertTrue(rec.duration <= baseline * SleepCalculatorTestFixtures.WAKE_WINDOW_BOUND_MAX)
    }

    @Test
    fun `computeAdaptiveWakeWindow returns baseline when single session has zero duration`() = runTest {
        val sessions = listOf(session(0))
        val calculator = createCalculator(sessions = sessions)
        val rec = calculator.computeAdaptiveWakeWindow("baby1", ageInWeeks = 12)
        assertEquals(75.minutes, rec.duration)
        assertNull(rec.adjustmentReason)
    }

    @Test
    fun `detectTimezoneChange returns null when offset difference is 1 hour or less`() = runTest {
        val sessions = listOf(session(45, timezoneOffset = 60))
        val calculator = createCalculator(sessions = sessions)
        assertNull(calculator.detectTimezoneChange("baby1", currentTimezoneOffsetMinutes = 120))
        assertNull(calculator.detectTimezoneChange("baby1", currentTimezoneOffsetMinutes = 60))
    }

    @Test
    fun `detectTimezoneChange returns adjustment when offset difference exceeds 1 hour`() = runTest {
        val sessions = listOf(session(45, timezoneOffset = 0))
        val calculator = createCalculator(sessions = sessions)
        val adj = calculator.detectTimezoneChange("baby1", currentTimezoneOffsetMinutes = 180)
        assertTrue(adj != null)
        assertEquals(3, adj.hoursDifference)
        assertEquals(6, adj.estimatedDays)
        assertEquals(6, adj.adjustmentSchedule.size)
    }

    @Test
    fun `buildTimezoneAdjustmentSchedule produces 30-minute daily increments`() = runTest {
        val calculator = createCalculator()
        val schedule = calculator.buildTimezoneAdjustmentSchedule(2)
        assertEquals(4, schedule.size)
        assertEquals(30.minutes, schedule[0])
        assertEquals(60.minutes, schedule[1])
        assertEquals(90.minutes, schedule[2])
        assertEquals(120.minutes, schedule[3])
    }

    @Test
    fun `buildTimezoneAdjustmentSchedule for 0 hours returns empty list`() = runTest {
        val calculator = createCalculator()
        val schedule = calculator.buildTimezoneAdjustmentSchedule(0)
        assertTrue(schedule.isEmpty())
    }

    @Test
    fun `buildTimezoneAdjustmentSchedule for 3 hours gives 6 steps`() = runTest {
        val calculator = createCalculator()
        val schedule = calculator.buildTimezoneAdjustmentSchedule(3)
        assertEquals(6, schedule.size)
        assertEquals(180.minutes, schedule.last())
    }

    @Test
    fun `detectTimezoneChange returns null when no sessions`() = runTest {
        val calculator = createCalculator()
        assertNull(calculator.detectTimezoneChange("baby1", currentTimezoneOffsetMinutes = 120))
    }
}
