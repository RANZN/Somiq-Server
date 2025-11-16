package com.ranjan.data.di

import com.ranjan.data.auth.repository.RefreshTokenRepoImpl
import com.ranjan.data.auth.repository.UserRepositoryImpl
import com.ranjan.data.auth.service.JwtTokenProvider
import com.ranjan.data.auth.service.PasswordCipherImpl
import com.ranjan.domain.auth.repository.RefreshTokenRepo
import com.ranjan.domain.auth.repository.UserRepository
import com.ranjan.domain.auth.services.PasswordCipher
import com.ranjan.domain.auth.services.TokenProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    singleOf<UserRepository>(::UserRepositoryImpl)
    singleOf<RefreshTokenRepo>(::RefreshTokenRepoImpl)
    singleOf<TokenProvider>(::JwtTokenProvider)
    singleOf<PasswordCipher>(::PasswordCipherImpl)
}
