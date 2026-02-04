package com.somni.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class BabyProfile(
    val babyId: String,
    val name: String,
    val birthdate: LocalDate,
    val createdAt: Instant,
) {
    fun ageInWeeks(): Int {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return (today.toEpochDays() - birthdate.toEpochDays()) / 7
    }

    fun ageInMonths(): Int = ageInWeeks() / 4
}
