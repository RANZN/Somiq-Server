package com.ranjan.domain.di

import com.ranjan.domain.account.usecase.GetProfileUseCase
import com.ranjan.domain.account.usecase.ToggleFollowUseCase
import com.ranjan.domain.account.usecase.UpdateProfileUseCase
import com.ranjan.domain.auth.usecase.ForgotPasswordUseCase
import com.ranjan.domain.auth.usecase.LoginUserUseCase
import com.ranjan.domain.auth.usecase.LogoutUseCase
import com.ranjan.domain.auth.usecase.SignUpUserUseCase
import com.ranjan.domain.post.usecase.CreatePostUseCase
import com.ranjan.domain.post.usecase.DeletePostUseCase
import com.ranjan.domain.post.usecase.GetPostByIdUseCase
import com.ranjan.domain.post.usecase.GetPostsUseCase
import com.ranjan.domain.post.usecase.ToggleBookmarkUseCase
import com.ranjan.domain.post.usecase.ToggleLikeUseCase
import com.ranjan.domain.post.usecase.UpdatePostUseCase
import com.ranjan.domain.reel.usecase.CreateReelUseCase
import com.ranjan.domain.reel.usecase.DeleteReelUseCase
import com.ranjan.domain.reel.usecase.GetReelByIdUseCase
import com.ranjan.domain.reel.usecase.GetReelsUseCase
import com.ranjan.domain.reel.usecase.RecordReelViewUseCase
import com.ranjan.domain.reel.usecase.ToggleReelBookmarkUseCase
import com.ranjan.domain.reel.usecase.ToggleReelLikeUseCase
import com.ranjan.domain.reel.usecase.UpdateReelUseCase
import com.ranjan.domain.search.usecase.SearchUseCase
import com.ranjan.domain.collection.usecase.AddItemToCollectionUseCase
import com.ranjan.domain.collection.usecase.CreateCollectionUseCase
import com.ranjan.domain.collection.usecase.DeleteCollectionUseCase
import com.ranjan.domain.collection.usecase.GetCollectionItemsUseCase
import com.ranjan.domain.collection.usecase.GetCollectionsUseCase
import com.ranjan.domain.collection.usecase.RemoveItemFromCollectionUseCase
import com.ranjan.domain.collection.usecase.UpdateCollectionUseCase
import com.ranjan.domain.comment.usecase.CreateCommentUseCase
import com.ranjan.domain.comment.usecase.DeleteCommentUseCase
import com.ranjan.domain.comment.usecase.GetCommentsUseCase
import com.ranjan.domain.comment.usecase.ToggleCommentLikeUseCase
import com.ranjan.domain.comment.usecase.UpdateCommentUseCase
import com.ranjan.domain.notification.usecase.GetNotificationsUseCase
import com.ranjan.domain.notification.usecase.GetUnreadCountUseCase
import com.ranjan.domain.notification.usecase.MarkAllNotificationsReadUseCase
import com.ranjan.domain.notification.usecase.MarkNotificationReadUseCase
import com.ranjan.domain.story.usecase.CreateStoryUseCase
import com.ranjan.domain.story.usecase.DeleteStoryUseCase
import com.ranjan.domain.story.usecase.GetStoriesFeedUseCase
import com.ranjan.domain.story.usecase.GetUserStoriesUseCase
import com.ranjan.domain.story.usecase.RecordStoryViewUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::LoginUserUseCase)
    factoryOf(::SignUpUserUseCase)
    factoryOf(::ForgotPasswordUseCase)
    factoryOf(::LogoutUseCase)

    factoryOf(::CreatePostUseCase)
    factoryOf(::DeletePostUseCase)
    factoryOf(::GetPostByIdUseCase)
    factoryOf(::GetPostsUseCase)
    factoryOf(::ToggleBookmarkUseCase)
    factoryOf(::ToggleLikeUseCase)
    factoryOf(::UpdatePostUseCase)

    factoryOf(::CreateReelUseCase)
    factoryOf(::DeleteReelUseCase)
    factoryOf(::GetReelByIdUseCase)
    factoryOf(::GetReelsUseCase)
    factoryOf(::ToggleReelBookmarkUseCase)
    factoryOf(::ToggleReelLikeUseCase)
    factoryOf(::UpdateReelUseCase)
    factoryOf(::RecordReelViewUseCase)

    factoryOf(::SearchUseCase)

    factoryOf(::CreateCommentUseCase)
    factoryOf(::DeleteCommentUseCase)
    factoryOf(::GetCommentsUseCase)
    factoryOf(::ToggleCommentLikeUseCase)
    factoryOf(::UpdateCommentUseCase)

    factoryOf(::GetNotificationsUseCase)
    factoryOf(::MarkNotificationReadUseCase)
    factoryOf(::MarkAllNotificationsReadUseCase)
    factoryOf(::GetUnreadCountUseCase)

    factoryOf(::CreateStoryUseCase)
    factoryOf(::DeleteStoryUseCase)
    factoryOf(::GetStoriesFeedUseCase)
    factoryOf(::GetUserStoriesUseCase)
    factoryOf(::RecordStoryViewUseCase)

    factoryOf(::CreateCollectionUseCase)
    factoryOf(::DeleteCollectionUseCase)
    factoryOf(::GetCollectionsUseCase)
    factoryOf(::GetCollectionItemsUseCase)
    factoryOf(::AddItemToCollectionUseCase)
    factoryOf(::RemoveItemFromCollectionUseCase)
    factoryOf(::UpdateCollectionUseCase)

    factoryOf(::GetProfileUseCase)
    factoryOf(::UpdateProfileUseCase)
    factoryOf(::ToggleFollowUseCase)
}