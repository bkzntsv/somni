package com.somni.calculator

import com.somni.domain.model.WakeWindowRecommendation

interface NotificationScheduler {
    suspend fun scheduleWakeWindowNotification(recommendation: WakeWindowRecommendation)
}
