package com.ranjan.di

import com.ranjan.application.auth.AuthController
import com.ranjan.data.repository.RefreshTokenRepoImpl
import com.ranjan.data.repository.UserRepositoryImpl
import com.ranjan.data.service.JwtTokenProvider
import com.ranjan.data.service.PasswordCipherImpl
import com.ranjan.domain.auth.repository.RefreshTokenRepo
import com.ranjan.domain.auth.repository.UserRepository
import com.ranjan.domain.common.services.TokenProvider
import com.ranjan.domain.common.services.PasswordCipher
import com.ranjan.domain.auth.usecase.LoginUserUseCase
import com.ranjan.domain.auth.usecase.SignUpUserUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::AuthController)
    singleOf(::LoginUserUseCase)
    singleOf(::SignUpUserUseCase)
    singleOf<UserRepository>(::UserRepositoryImpl)
    singleOf<RefreshTokenRepo>(::RefreshTokenRepoImpl)
    singleOf<PasswordCipher>(::PasswordCipherImpl)
    singleOf<TokenProvider>(::JwtTokenProvider)
}