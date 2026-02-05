package com.somni.di

import com.somni.database.SomniDatabase
import com.somni.database.createSqlDriver
import com.somni.domain.repository.BabyProfileRepository
import com.somni.domain.repository.SleepRepository
import com.somni.storage.LocalStorage
import org.koin.core.module.Module
import org.koin.dsl.module

val commonModule = module {
    single<SomniDatabase> {
        val driver = createSqlDriver(databaseName = "somni.db", passphrase = null)
        SomniDatabase(driver)
    }
    single<SleepRepository> { get<LocalStorage>() }
    single<BabyProfileRepository> { get<LocalStorage>() }
    single {
        LocalStorage(get())
    }
}

fun getCommonModules(): List<Module> = listOf(commonModule)
