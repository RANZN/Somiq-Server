package com.ranjan.core.di

import com.ranjan.core.util.SystemTimeProvider
import com.ranjan.core.util.TimeProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreModule = module {
    singleOf(::SystemTimeProvider) bind TimeProvider::class
}
