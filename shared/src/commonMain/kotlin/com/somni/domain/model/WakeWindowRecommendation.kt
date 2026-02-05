package com.somni.domain.model

import kotlin.time.Duration
import kotlinx.datetime.Instant

data class WakeWindowRecommendation(
    val duration: Duration,
    val adjustmentReason: String?,
    val confidence: Float,
    val nextSleepTime: Instant,
    val baselineWindow: Duration,
    val adjustmentMultiplier: Float,
)
