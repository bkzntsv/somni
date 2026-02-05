package com.somni.calculator

import com.somni.domain.model.SleepSession
import com.somni.domain.model.TimezoneAdjustment
import com.somni.domain.model.WakeWindowRecommendation
import com.somni.domain.repository.SleepRepository
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class SleepCalculator(
    private val sleepRepository: SleepRepository,
    private val notificationScheduler: NotificationScheduler,
    private val timeProvider: TimeProvider = SystemTimeProvider,
) {
    fun computeBaselineWakeWindow(ageInWeeks: Int): Duration {
        require(ageInWeeks >= 0) { "ageInWeeks must be non-negative" }
        return when {
            ageInWeeks <= 4 -> 45.minutes
            ageInWeeks <= 8 -> 60.minutes
            ageInWeeks <= 12 -> 75.minutes
            ageInWeeks <= 16 -> 90.minutes
            ageInWeeks <= 24 -> 120.minutes
            ageInWeeks <= 36 -> 150.minutes
            ageInWeeks <= 52 -> 180.minutes
            else -> 240.minutes
        }
    }

    suspend fun computeAdaptiveWakeWindow(
        babyId: String,
        ageInWeeks: Int,
        @Suppress("UNUSED_PARAMETER") currentTimezoneOffsetMinutes: Int = 0,
    ): WakeWindowRecommendation {
        require(ageInWeeks >= 0) { "ageInWeeks must be non-negative" }
        val baseline = computeBaselineWakeWindow(ageInWeeks)
        val recentSessions = sleepRepository.getSessions(babyId, days = 14)
            .filter { it.endTime != null && it.durationMinutes != null }
            .sortedByDescending { it.endTime }
            .take(REQUIRED_SESSIONS_FOR_QUALITY)

        val (adjustedDuration, reason, confidence) = when {
            recentSessions.size < MIN_SESSIONS_FOR_ADJUSTMENT -> Triple(
                baseline,
                null as String?,
                BASELINE_CONFIDENCE,
            )
            else -> computeAdjustmentFromQuality(baseline, recentSessions)
        }

        val clampedDuration = clampToBounds(adjustedDuration, baseline)
        val lastSession = recentSessions.firstOrNull()
        val wakeTime = lastSession?.endTime ?: timeProvider.now()
        val nextSleepTime = wakeTime.plus(clampedDuration)

        return WakeWindowRecommendation(
            duration = clampedDuration,
            adjustmentReason = reason,
            confidence = confidence,
            nextSleepTime = nextSleepTime,
            baselineWindow = baseline,
            adjustmentMultiplier = clampedDuration.inWholeMinutes.toFloat() / baseline.inWholeMinutes.toFloat(),
        )
    }

    suspend fun detectTimezoneChange(
        babyId: String,
        currentTimezoneOffsetMinutes: Int,
        currentTimezoneId: String = "",
    ): TimezoneAdjustment? {
        val recentSessions = sleepRepository.getSessions(babyId, days = 7)
            .filter { it.endTime != null }
            .sortedByDescending { it.modifiedAt }
        val lastSession = recentSessions.firstOrNull() ?: return null
        val lastOffset = lastSession.timezoneOffset
        val diffMinutes = kotlin.math.abs(currentTimezoneOffsetMinutes - lastOffset)
        if (diffMinutes <= TIMEZONE_CHANGE_THRESHOLD_MINUTES) return null
        val hoursDifference = (currentTimezoneOffsetMinutes - lastOffset) / 60
        val schedule = buildTimezoneAdjustmentSchedule(kotlin.math.abs(hoursDifference))
        return TimezoneAdjustment(
            oldTimezone = "UTC${formatOffset(lastOffset)}",
            newTimezone = currentTimezoneId.ifBlank { "UTC${formatOffset(currentTimezoneOffsetMinutes)}" },
            hoursDifference = hoursDifference,
            adjustmentSchedule = schedule,
            estimatedDays = schedule.size,
        )
    }

    fun buildTimezoneAdjustmentSchedule(hoursDifference: Int): List<Duration> {
        require(hoursDifference >= 0) { "hoursDifference must be non-negative for schedule" }
        val totalMinutes = hoursDifference * 60
        val numberOfDays = (totalMinutes + INCREMENT_MINUTES - 1) / INCREMENT_MINUTES
        return List(numberOfDays) { ((it + 1) * INCREMENT_MINUTES).minutes }
    }

    suspend fun scheduleWakeWindowNotification(recommendation: WakeWindowRecommendation) {
        notificationScheduler.scheduleWakeWindowNotification(recommendation)
    }

    private fun computeQualityRatio(sessions: List<SleepSession>): Double {
        val durations = sessions.map { it.durationMinutes!!.toLong().minutes }
        val expectedMinutes = durations.map { it.inWholeMinutes }.average()
        val lastDuration = durations.first().inWholeMinutes.toDouble()
        return if (expectedMinutes == 0.0) 1.0 else lastDuration / expectedMinutes
    }

    private fun computeReduceMultiplier(ratio: Double): Float {
        val factor = 1f - (ratio / POOR_QUALITY_THRESHOLD).toFloat()
        return 1f - (REDUCE_MIN + (REDUCE_MAX - REDUCE_MIN) * factor)
    }

    private fun computeIncreaseMultiplier(ratio: Double): Float {
        val excessRatio = ((ratio - EXCELLENT_QUALITY_THRESHOLD) / 0.5).toFloat().coerceIn(0f, 1f)
        return 1f + (INCREASE_MIN + (INCREASE_MAX - INCREASE_MIN) * excessRatio)
    }

    private fun computeAdjustmentFromQuality(
        baseline: Duration,
        sessions: List<SleepSession>,
    ): Triple<Duration, String?, Float> {
        val ratio = computeQualityRatio(sessions)
        return when {
            ratio < POOR_QUALITY_THRESHOLD -> {
                val multiplier = computeReduceMultiplier(ratio)
                    .coerceIn(BOUNDS_MIN_MULTIPLIER.toFloat(), BOUNDS_MAX_MULTIPLIER.toFloat())
                Triple(
                    baseline * multiplier.toDouble(),
                    "Recent sleep shorter than expected; reducing wake window.",
                    ADAPTIVE_CONFIDENCE,
                )
            }
            ratio > EXCELLENT_QUALITY_THRESHOLD -> {
                val multiplier = computeIncreaseMultiplier(ratio)
                    .coerceIn(BOUNDS_MIN_MULTIPLIER.toFloat(), BOUNDS_MAX_MULTIPLIER.toFloat())
                Triple(
                    baseline * multiplier.toDouble(),
                    "Recent sleep longer than expected; increasing wake window.",
                    ADAPTIVE_CONFIDENCE,
                )
            }
            else -> Triple(baseline, null, BASELINE_CONFIDENCE)
        }
    }

    private fun clampToBounds(adjusted: Duration, baseline: Duration): Duration {
        val lower = baseline * BOUNDS_MIN_MULTIPLIER
        val upper = baseline * BOUNDS_MAX_MULTIPLIER
        return when {
            adjusted < lower -> lower
            adjusted > upper -> upper
            else -> adjusted
        }
    }

    private fun formatOffset(offsetMinutes: Int): String {
        val sign = if (offsetMinutes >= 0) "+" else "-"
        val absOffset = kotlin.math.abs(offsetMinutes)
        val hours = absOffset / 60
        val minutes = absOffset % 60
        return if (minutes == 0) "$sign$hours" else "$sign${hours}:${minutes.toString().padStart(2, '0')}"
    }

    private companion object {
        const val REQUIRED_SESSIONS_FOR_QUALITY = 5
        const val MIN_SESSIONS_FOR_ADJUSTMENT = 1
        const val POOR_QUALITY_THRESHOLD = 0.70
        const val EXCELLENT_QUALITY_THRESHOLD = 1.10
        const val REDUCE_MIN = 0.10f
        const val REDUCE_MAX = 0.15f
        const val INCREASE_MIN = 0.05f
        const val INCREASE_MAX = 0.10f
        val BOUNDS_MIN_MULTIPLIER = 0.70
        val BOUNDS_MAX_MULTIPLIER = 1.30
        const val BASELINE_CONFIDENCE = 0.85f
        const val ADAPTIVE_CONFIDENCE = 0.75f
        const val TIMEZONE_CHANGE_THRESHOLD_MINUTES = 60
        const val INCREMENT_MINUTES = 30
    }
}

private object SystemTimeProvider : TimeProvider {
    override fun now(): Instant = kotlinx.datetime.Clock.System.now()
}
