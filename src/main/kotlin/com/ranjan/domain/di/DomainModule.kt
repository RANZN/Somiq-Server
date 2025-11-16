package com.ranjan.domain.di

import com.ranjan.domain.auth.usecase.LoginUserUseCase
import com.ranjan.domain.auth.usecase.SignUpUserUseCase
import com.ranjan.domain.post.usecase.CreatePostUseCase
import com.ranjan.domain.post.usecase.DeletePostUseCase
import com.ranjan.domain.post.usecase.GetPostByIdUseCase
import com.ranjan.domain.post.usecase.GetPostsUseCase
import com.ranjan.domain.post.usecase.ToggleBookmarkUseCase
import com.ranjan.domain.post.usecase.ToggleLikeUseCase
import com.ranjan.domain.post.usecase.UpdatePostUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::LoginUserUseCase)
    factoryOf(::SignUpUserUseCase)
    factoryOf(::CreatePostUseCase)
    factoryOf(::DeletePostUseCase)
    factoryOf(::GetPostByIdUseCase)
    factoryOf(::GetPostsUseCase)
    factoryOf(::ToggleBookmarkUseCase)
    factoryOf(::ToggleLikeUseCase)
    factoryOf(::UpdatePostUseCase)
}