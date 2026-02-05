package com.somni.calculator

import kotlinx.datetime.Instant

interface TimeProvider {
    fun now(): Instant
}
