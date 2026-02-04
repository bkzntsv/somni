package com.somni.di

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(getIosModules())
    }
}
