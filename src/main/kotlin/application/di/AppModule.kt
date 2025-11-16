package com.ranjan.application.di

import com.ranjan.application.auth.AuthController
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::AuthController)
}