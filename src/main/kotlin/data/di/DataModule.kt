package com.ranjan.data.di

import com.ranjan.data.auth.repository.RefreshTokenRepoImpl
import com.ranjan.data.auth.repository.UserRepositoryImpl
import com.ranjan.data.auth.service.JwtTokenProvider
import com.ranjan.data.auth.service.PasswordCipherImpl
import com.ranjan.data.post.repository.PostRepositoryImpl
import com.ranjan.data.sources.db.DataSourceProvider
import com.ranjan.data.util.SystemTimeProvider
import com.ranjan.data.util.TimeProvider
import com.ranjan.domain.auth.repository.RefreshTokenRepo
import com.ranjan.domain.auth.repository.UserRepository
import com.ranjan.domain.auth.services.PasswordCipher
import com.ranjan.domain.auth.services.TokenProvider
import com.ranjan.domain.post.repository.PostRepository
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

val dataModule = module {
    single<Database> { DataSourceProvider.initDatabase() }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<RefreshTokenRepo> { RefreshTokenRepoImpl(get()) }
    single<TokenProvider> { JwtTokenProvider() }
    single<PasswordCipher> { PasswordCipherImpl() }
    single<PostRepository> { PostRepositoryImpl(get(), get()) }
    single<TimeProvider> { SystemTimeProvider() }
}
