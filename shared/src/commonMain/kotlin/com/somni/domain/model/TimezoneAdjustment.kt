package com.somni.domain.model

import kotlin.time.Duration

data class TimezoneAdjustment(
    val oldTimezone: String,
    val newTimezone: String,
    val hoursDifference: Int,
    val adjustmentSchedule: List<Duration>,
    val estimatedDays: Int,
)
