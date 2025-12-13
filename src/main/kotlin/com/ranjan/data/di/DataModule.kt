package com.ranjan.data.di

import com.ranjan.data.account.repository.AccountRepositoryImpl
import com.ranjan.data.auth.repository.RefreshTokenRepoImpl
import com.ranjan.data.auth.repository.UserRepositoryImpl
import com.ranjan.data.auth.service.JwtTokenProvider
import com.ranjan.data.auth.service.PasswordCipherImpl
import com.ranjan.data.collection.repository.CollectionRepositoryImpl
import com.ranjan.data.comment.repository.CommentRepositoryImpl
import com.ranjan.data.notification.repository.NotificationRepositoryImpl
import com.ranjan.data.post.repository.PostRepositoryImpl
import com.ranjan.data.reel.repository.ReelRepositoryImpl
import com.ranjan.data.search.repository.SearchRepositoryImpl
import com.ranjan.data.story.repository.StoryRepositoryImpl
import com.ranjan.data.sources.db.DataSourceProvider
import com.ranjan.data.util.SystemTimeProvider
import com.ranjan.data.util.TimeProvider
import com.ranjan.domain.account.repository.AccountRepository
import com.ranjan.domain.auth.repository.RefreshTokenRepo
import com.ranjan.domain.auth.repository.UserRepository
import com.ranjan.domain.auth.services.PasswordCipher
import com.ranjan.domain.auth.services.TokenProvider
import com.ranjan.domain.collection.repository.CollectionRepository
import com.ranjan.domain.comment.repository.CommentRepository
import com.ranjan.domain.notification.repository.NotificationRepository
import com.ranjan.domain.post.repository.PostRepository
import com.ranjan.domain.reel.repository.ReelRepository
import com.ranjan.domain.search.repository.SearchRepository
import com.ranjan.domain.story.repository.StoryRepository
import org.jetbrains.exposed.sql.Database
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    single<Database> { DataSourceProvider.initDatabase() }
    singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
    singleOf(::RefreshTokenRepoImpl) { bind<RefreshTokenRepo>() }
    singleOf(::JwtTokenProvider) { bind<TokenProvider>() }
    singleOf(::PasswordCipherImpl) { bind<PasswordCipher>() }
    singleOf(::PostRepositoryImpl) { bind<PostRepository>() }
    singleOf(::ReelRepositoryImpl) { bind<ReelRepository>() }
    singleOf(::CommentRepositoryImpl) { bind<CommentRepository>() }
    singleOf(::NotificationRepositoryImpl) { bind<NotificationRepository>() }
    singleOf(::StoryRepositoryImpl) { bind<StoryRepository>() }
    singleOf(::CollectionRepositoryImpl) { bind<CollectionRepository>() }
    singleOf(::SearchRepositoryImpl) { bind<SearchRepository>() }
    singleOf(::AccountRepositoryImpl) { bind<AccountRepository>() }
    singleOf(::SystemTimeProvider) { bind<TimeProvider>() }
}
