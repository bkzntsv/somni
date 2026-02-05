package com.somni.calculator

import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class WakeWindowBoundsPropertyTest {

    private val seed: Long = 42L

    private fun createCalculator(
        sessions: List<com.somni.domain.model.SleepSession>,
        timeProvider: TimeProvider = object : TimeProvider {
            override fun now() = SleepCalculatorTestFixtures.fixedInstant
        },
    ) = SleepCalculatorTestFixtures.createCalculator(sessions, timeProvider)

    private fun session(durationMinutes: Int, uniqueId: Long = Random(seed).nextLong()) =
        SleepCalculatorTestFixtures.session(durationMinutes, sessionId = "s-$durationMinutes-$uniqueId")

    @Test
    fun `adjusted wake windows stay within 30 percent of baseline for any age 0-99 weeks`() = runTest {
        val rng = Random(seed)
        val iterations = 100
        repeat(iterations) { i ->
            val ageInWeeks = rng.nextInt(0, 100)
            val calculator = createCalculator(sessions = emptyList())
            val rec = calculator.computeAdaptiveWakeWindow("baby1", ageInWeeks)
            val baseline = calculator.computeBaselineWakeWindow(ageInWeeks)
            val lower = baseline * SleepCalculatorTestFixtures.WAKE_WINDOW_BOUND_MIN
            val upper = baseline * SleepCalculatorTestFixtures.WAKE_WINDOW_BOUND_MAX
            assertTrue(
                rec.duration >= lower,
                "Seed=$seed iteration=$i ageInWeeks=$ageInWeeks: duration ${rec.duration.inWholeMinutes} min should be >= ${lower.inWholeMinutes} (70% of baseline ${baseline.inWholeMinutes})",
            )
            assertTrue(
                rec.duration <= upper,
                "Seed=$seed iteration=$i ageInWeeks=$ageInWeeks: duration ${rec.duration.inWholeMinutes} min should be <= ${upper.inWholeMinutes} (130% of baseline ${baseline.inWholeMinutes})",
            )
        }
    }

    @Test
    fun `adjusted wake windows stay within 30 percent of baseline with random session quality`() = runTest {
        val rng = Random(seed)
        val iterations = 80
        repeat(iterations) { i ->
            val ageInWeeks = rng.nextInt(0, 52)
            val sessionCount = rng.nextInt(1, 5)
            val sessions = List(sessionCount) {
                val durationMinutes = rng.nextInt(10, 120)
                SleepCalculatorTestFixtures.session(
                    durationMinutes,
                    sessionId = "s-$durationMinutes-${rng.nextLong()}",
                )
            }
            val calculator = createCalculator(sessions = sessions)
            val rec = calculator.computeAdaptiveWakeWindow("baby1", ageInWeeks)
            val baseline = calculator.computeBaselineWakeWindow(ageInWeeks)
            val lower = baseline * SleepCalculatorTestFixtures.WAKE_WINDOW_BOUND_MIN
            val upper = baseline * SleepCalculatorTestFixtures.WAKE_WINDOW_BOUND_MAX
            assertTrue(
                rec.duration >= lower,
                "Seed=$seed iteration=$i ageInWeeks=$ageInWeeks sessions=${sessions.map { it.durationMinutes }}: duration ${rec.duration.inWholeMinutes} >= ${lower.inWholeMinutes}",
            )
            assertTrue(
                rec.duration <= upper,
                "Seed=$seed iteration=$i ageInWeeks=$ageInWeeks sessions=${sessions.map { it.durationMinutes }}: duration ${rec.duration.inWholeMinutes} <= ${upper.inWholeMinutes}",
            )
        }
    }

    @Test
    fun `adjusted wake windows stay within bounds for extreme quality scores`() = runTest {
        val ageInWeeks = 12
        val baseline = 75.minutes
        val rng = Random(seed)
        val cases = listOf(
            listOf(session(5, rng.nextLong()), session(5, rng.nextLong()), session(5, rng.nextLong())),
            listOf(session(120, rng.nextLong()), session(120, rng.nextLong()), session(120, rng.nextLong())),
        )
        for (sessions in cases) {
            val calculator = createCalculator(sessions = sessions)
            val rec = calculator.computeAdaptiveWakeWindow("baby1", ageInWeeks)
            assertTrue(rec.duration >= baseline * SleepCalculatorTestFixtures.WAKE_WINDOW_BOUND_MIN)
            assertTrue(rec.duration <= baseline * SleepCalculatorTestFixtures.WAKE_WINDOW_BOUND_MAX)
        }
    }
}
