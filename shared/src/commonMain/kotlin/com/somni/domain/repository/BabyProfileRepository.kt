package com.somni.domain.repository

import com.somni.domain.model.BabyProfile

interface BabyProfileRepository {
    suspend fun getAllProfiles(): List<BabyProfile>
    suspend fun getProfile(babyId: String): BabyProfile?
    suspend fun getActiveProfile(userId: String): BabyProfile?
    suspend fun insertProfile(profile: BabyProfile)
    suspend fun updateProfile(profile: BabyProfile)
    suspend fun deleteProfile(babyId: String)
    suspend fun setActiveProfile(userId: String, babyId: String)
}
