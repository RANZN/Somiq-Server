package com.ranjan.domain.di

import com.ranjan.domain.auth.usecase.LoginUserUseCase
import com.ranjan.domain.auth.usecase.SignUpUserUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::LoginUserUseCase)
    factoryOf(::SignUpUserUseCase)
}