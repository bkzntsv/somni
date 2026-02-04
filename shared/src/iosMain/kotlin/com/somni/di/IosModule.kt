package com.somni.di

import org.koin.core.module.Module
import org.koin.dsl.module

val iosModule = module {}

fun getIosModules(): List<Module> = getCommonModules() + listOf(iosModule)
