package com.ranjan.application.di

import com.ranjan.application.auth.AuthController
import com.ranjan.application.post.PostController
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::AuthController)
    singleOf(::PostController)
}