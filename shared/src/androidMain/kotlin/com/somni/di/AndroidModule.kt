package com.somni.di

import org.koin.core.module.Module
import org.koin.dsl.module

val androidModule = module {
}

fun getAndroidModules(): List<Module> = getCommonModules() + listOf(
    androidModule
)
