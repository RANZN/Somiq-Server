package com.ranjan.server.di

import com.ranjan.server.auth.AuthController
import com.ranjan.server.post.PostController
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::AuthController)
    singleOf(::PostController)
}