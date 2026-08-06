package com.ranjan.chat.di

import com.ranjan.chat.data.datasource.ConversationDataSource
import com.ranjan.chat.data.datasource.MessageDataSource
import com.ranjan.chat.data.repository.ChatRepositoryImpl
import com.ranjan.chat.domain.repository.ChatRepository
import com.ranjan.chat.domain.usecase.CreateConversationUseCase
import com.ranjan.chat.domain.usecase.GetMessagesUseCase
import com.ranjan.chat.domain.usecase.MarkAsReadUseCase
import com.ranjan.chat.domain.usecase.SendMessageUseCase
import com.ranjan.chat.websocket.ChatConnectionManager
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val chatModule = module {
    // DataSources
    singleOf(::ConversationDataSource)
    singleOf(::MessageDataSource)

    // Repositories
    singleOf(::ChatRepositoryImpl) { bind<ChatRepository>() }

    // UseCases
    singleOf(::CreateConversationUseCase)
    singleOf(::SendMessageUseCase)
    singleOf(::GetMessagesUseCase)
    singleOf(::MarkAsReadUseCase)

    // WebSocket
    singleOf(::ChatConnectionManager)
}
