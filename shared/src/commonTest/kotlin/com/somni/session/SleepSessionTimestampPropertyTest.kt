package com.somni.session

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class SleepSessionTimestampPropertyTest {

    private val seed = 42L
    private val baseInstant = Instant.fromEpochMilliseconds(1_700_000_000_000L)

    @Test
    fun `durationMinutes equals endTime minus startTime for various durations`() = runTest {
        val rng = Random(seed)
        repeat(100) { i ->
            val durationMinutes = rng.nextInt(1, 480)
            val startTime = baseInstant.plus((i * 1000).toLong().minutes)
            val endTime = startTime.plus(durationMinutes.minutes)
            val (manager, timeSequence) = SleepSessionManagerTestFixtures.createManagerWithSequentialTime(
                startInstant = startTime,
                sessionIdGenerator = object : SessionIdGenerator {
                    override fun generate(): String = "prop-$i"
                },
            )
            manager.startSleepSession("baby1", "device1")
            timeSequence.add(endTime)

            val completed = manager.endSleepSession("prop-$i")

            val expectedMinutes = (completed.endTime!! - completed.startTime).inWholeMinutes.toInt()
            assertEquals(
                expectedMinutes,
                completed.durationMinutes,
                "Iteration $i: durationMinutes should match (endTime - startTime).inWholeMinutes",
            )
        }
    }

    @Test
    fun `updateSession preserves duration integrity for manual adjustments`() = runTest {
        val rng = Random(seed)
        repeat(50) { i ->
            val originalDuration = rng.nextInt(30, 180)
            val adjustmentMinutes = rng.nextInt(-15, 15)
            val startTime = baseInstant.plus((i * 500).toLong().minutes)
            val endTime = startTime.plus(originalDuration.minutes)
            val session = SleepSessionManagerTestFixtures.session(
                sessionId = "adj-$i",
                startTime = startTime,
                endTime = endTime,
                durationMinutes = originalDuration,
            )
            val manager = SleepSessionManagerTestFixtures.createManager(initialSessions = listOf(session))
            val newEndTime = startTime.plus((originalDuration + adjustmentMinutes).coerceAtLeast(1).minutes)

            val updated = manager.updateSession("adj-$i", startTime = null, endTime = newEndTime)

            val expectedMinutes = (updated.endTime!! - updated.startTime).inWholeMinutes.toInt()
            assertEquals(
                expectedMinutes,
                updated.durationMinutes,
                "Iteration $i: manual adjustment should preserve duration = (endTime - startTime).inWholeMinutes",
            )
        }
    }
}
