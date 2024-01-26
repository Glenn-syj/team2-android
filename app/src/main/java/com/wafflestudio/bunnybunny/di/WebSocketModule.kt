package com.wafflestudio.bunnybunny.di

import android.content.Context
import android.content.SharedPreferences
import com.wafflestudio.bunnybunny.lib.network.BunnyWebSocketListener
import com.wafflestudio.bunnybunny.lib.network.WebServicesProvider
import com.wafflestudio.bunnybunny.viewModel.MainViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class WebSocketModule {
    @Provides
    @Singleton
    @Named("WebSocketOkHttpClient")
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(39, TimeUnit.SECONDS)
            .build()    }

    @Provides
    fun provideWebSocketListener(): BunnyWebSocketListener {
        return BunnyWebSocketListener()    }
//    @Provides
//    fun provideWebSocketForChannel(
//        sharedPreference: SharedPreferences,
//        okHttpClient: OkHttpClient,
//        webSocketListener: BunnyWebSocketListener,
//        @ChannelId channelId: Long,
//    ): WebSocket {
//        val address = "43.202.236.170:8080"
//        val token = sharedPreference.getString("originalToken", "")
//        val webSocketUrl = "ws://$address/ws/channels/$channelId?token=$token"
//
//        val request = Request.Builder()
//            .url(webSocketUrl)
//            .build()
//
//        return okHttpClient.newWebSocket(request, webSocketListener)
//    }
//
//    @Provides
//    fun provideWebSocketForUser(
//        sharedPreference: SharedPreferences,
//        okHttpClient: OkHttpClient,
//        webSocketListener: BunnyWebSocketListener,
//    ): WebSocket {
//        val address = "43.202.236.170:8080"
//        val token = sharedPreference.getString("originalToken", "")
//        val webSocketUrl = "ws://$address/ws/users?token=$token"
//
//        val request = Request.Builder()
//            .url(webSocketUrl)
//            .build()
//
//        return okHttpClient.newWebSocket(request, webSocketListener)
//    }

    @Provides
    @Singleton
    fun provideWebServicesProvider(
        sharedPreference: SharedPreferences,
        @Named("WebSocketOkHttpClient") okHttpClient: OkHttpClient,
        webSocketListener: BunnyWebSocketListener
    ): WebServicesProvider {
        return WebServicesProvider(sharedPreference, okHttpClient, webSocketListener)    }}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ChannelId