package com.wafflestudio.bunnybunny.lib.network

import android.content.SharedPreferences
import android.util.Log
import com.wafflestudio.bunnybunny.data.example.RecentMessagesResponse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

interface WebSocketManager {
    fun createWebSocket(channelId: Long)
    fun disposeWebSocket()
    fun isWebSocketAvailable(): Boolean

    fun sendMessage(message: String)
}

class WebSocketManagerImpl @Inject constructor(
    private val sharedPreference: SharedPreferences,
    private val okHttpClient: OkHttpClient,
    private val webSocketListener: BunnyWebSocketListener,
): WebSocketManager {
    var webSocket: WebSocket? = null

    override fun createWebSocket(channelId: Long) {
        val address = "banibani.shop"
        val token = sharedPreference.getString("originalToken", "") // token 값 가져옴
        val webSocketUrl =
            "ws://$address/ws/channels/$channelId?token=$token"
        Log.d("WSP", "$webSocketUrl")
        val request = Request.Builder()
            .url(webSocketUrl)
            .build()
        webSocket = okHttpClient.newWebSocket(request, webSocketListener)
    }

    override fun disposeWebSocket() {
        webSocket?.close(1000, "Disconnected by client")
        webSocket = null
    }

    override fun isWebSocketAvailable(): Boolean {
        return webSocket != null
    }

    override fun sendMessage(message: String) {
        webSocket?.send(message)
    }
}


class WebServicesProvider @Inject constructor(
    private val messageStorage: MessageStorage,
    private val sharedPreference: SharedPreferences,
    @Named("WebSocketOkHttpClient") private val okHttpClient: OkHttpClient,
    private val webSocketListener: BunnyWebSocketListener,
    private val userWebsocketListener: BunnyUserWebSocketListener
) {

    val webSocketManager = WebSocketManagerImpl(sharedPreference, okHttpClient, webSocketListener)


    private val _messageState = MutableStateFlow("")
    val messageState : StateFlow<String> = _messageState.asStateFlow()

    fun connectChannel(channelId: Long) {
        webSocketManager.createWebSocket(channelId)
    }

    fun connectUser(): WebSocket {
        val address = "banibani.shop"
        val token = sharedPreference.getString("originalToken", "")
        val webSocketUrl = "ws://$address/ws/users?token=$token"

        val request = Request.Builder()
            .url(webSocketUrl)
            .build()

        return okHttpClient.newWebSocket(request, webSocketListener)
    }

    fun disconnectChannel(webSocket: WebSocket) {
        webSocket.close(1000, "Disconnected by client")
    }

    fun shutdown() {
        okHttpClient.dispatcher.executorService.shutdown()
    }


    fun sendRecentMessageRequest(cur: Int)  {
        // RECENT_MESSAGE 메시지 포맷 작성
        val formattedMessage = "RECENT_MESSAGE\ncur:$cur\n"
        webSocketManager.sendMessage(formattedMessage)
    }

    fun sendTextMessage(channelWebSocket: WebSocket, body: String) {
        // SEND_TEXT 메시지 포맷 작성
        val formattedMessage = "SEND_TEXT\n\n$body\n"

        // channelWebSocket을 사용하여 메시지 전송
        channelWebSocket.send(formattedMessage)
    }


}