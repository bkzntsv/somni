package com.somni.di

import org.koin.core.module.Module
import org.koin.dsl.module

val commonModule =
    module {
    }

fun getCommonModules(): List<Module> =
    listOf(
        commonModule,
    )
