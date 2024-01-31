package com.wafflestudio.bunnybunny.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wafflestudio.bunnybunny.data.example.ChatChannel
import com.wafflestudio.bunnybunny.data.example.ChatListResponse
import com.wafflestudio.bunnybunny.data.example.CreateChatRoomRequest
import com.wafflestudio.bunnybunny.data.example.Message
import com.wafflestudio.bunnybunny.data.example.PrefRepository
import com.wafflestudio.bunnybunny.data.example.RecentMessagesResponse
import com.wafflestudio.bunnybunny.lib.network.MessageStorage
import com.wafflestudio.bunnybunny.lib.network.WebServicesProvider
import com.wafflestudio.bunnybunny.lib.network.api.BunnyApi
import com.wafflestudio.bunnybunny.utils.fromStringToNewUserMessageResponse
import com.wafflestudio.bunnybunny.utils.fromStringToRecentMessagesResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageStorage: MessageStorage,
    private val api: BunnyApi,
    private val webServicesProvider: WebServicesProvider,
    private val prefRepository: PrefRepository,
) : ViewModel() {

    val recentMessages: StateFlow<String> = webServicesProvider.messageState


    val latestMessage: StateFlow<List<Message>> = messageStorage.latestMessage
        .map {
            fromStringToRecentMessagesResponse(it).messages
        }.stateIn(
            viewModelScope, SharingStarted.Eagerly, emptyList()
        )

    private val _messagesStateFlow = MutableStateFlow(emptyList<Message>())
    val messagesStateFlow: StateFlow<List<Message>> = _messagesStateFlow

    private val _chatListResponse =
        MutableStateFlow(ChatListResponse(listOf<ChatChannel>(), listOf<ChatChannel>()))
    val chatListResponse: StateFlow<ChatListResponse> = _chatListResponse.asStateFlow()

    private val _unreadMessagesStateFlow = MutableStateFlow(HashMap<Long, MutableList<Long>>())
    val unreadMessagesStateFlow: StateFlow<HashMap<Long, MutableList<Long>>> =
        _unreadMessagesStateFlow.asStateFlow()

    private val _webSocketStateFlow = MutableStateFlow<WebSocket?>(null)
    val webSocketStateFlow: StateFlow<WebSocket?> = _webSocketStateFlow.asStateFlow()

    private val _userWebSocketStateFlow = MutableStateFlow<WebSocket?>(null)
    val userWebSocketStateFlow: StateFlow<WebSocket?> = _webSocketStateFlow.asStateFlow()

    suspend fun connectToChatRoom(channelId: Long) {
        val webSocket = webServicesProvider.connectChannel(channelId)
        delay(300)
        _webSocketStateFlow.value = webSocket
    }

    suspend fun connectToUser() {
        val webSocket = webServicesProvider.connectUser()
        delay(300)
        _userWebSocketStateFlow.value = webSocket
    }


    fun disconnectFromChatRoom() {
        // 웹소켓 연결 종료
        _webSocketStateFlow.value?.let { webServicesProvider.disconnectChannel(it) }
        _webSocketStateFlow.value = null
    }

    fun sendRecentMessagesFormattedStringRequest(cur: Int) {
        try {
            val websocket = _webSocketStateFlow.value!!
            webServicesProvider.sendRecentMessageRequest(websocket, 255)
//            delay(200)
//            val text = messageStorage.latestMessage.value
//            Log.d("ChatVieWModel", text)
//            val response = fromStringToNewUserMessageResponse(text)
//            Log.d("ChatVieWModel", "${_messagesStateFlow.value}")
        } catch (e: Exception) {
            // Handle the exception, log it, or take appropriate action
            Log.e("ChatVieWModel", "Error getting recent messages: ${e.message}")
        }
    }

    suspend fun getUserMessage() {
        try {
            val websocket = _userWebSocketStateFlow.value!!
            delay(200)
            val text = messageStorage.userLatestMessage.value
            Log.d("ChatVieWModel", text)
            val response = fromStringToNewUserMessageResponse(text)
            val unreadMessagesInChannel = _unreadMessagesStateFlow.value
            if (unreadMessagesInChannel.get(response.channelId) == null) {
                unreadMessagesInChannel.put(response.channelId, mutableListOf(response.createdAt))
            } else {
                unreadMessagesInChannel.get(response.channelId)!!.add(response.createdAt)
            }
            Log.d("ChatVieWModel", "${_unreadMessagesStateFlow.value}")
        } catch (e: Exception) {
            // Handle the exception, log it, or take appropriate action
            Log.e("ChatVieWModel", "Error getting recent messages: ${e.message}")
        }
    }

    fun getUnreadMessagesInChannel(channelId: Long): Int {
        return _unreadMessagesStateFlow.value.size
    }

    suspend fun sendMessage(text: String) {
        _webSocketStateFlow.value?.let { websocket ->
            webServicesProvider.sendTextMessage(websocket, text)
            delay(200)
            sendRecentMessagesFormattedStringRequest(255)
        }
    }

    //HTTP
    fun getChannelList() {
        val token = getTokenHeader()!!
        Log.d("CTPV", token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.chatChannelRequest(token)
                _chatListResponse.value = response
                Log.d("CTPV", "try" + response.toString())
            } catch (e: HttpException) {
                val message = e.response()?.code().toString()
                Log.d("CTPV", "catch" + message)
            }
        }
    }

    suspend fun makeChatRoom(postId: Long) {
        api.makeChatRoomRequest(getTokenHeader()!!, CreateChatRoomRequest(postId))
    }

    fun getTokenHeader(): String? {
        //Log.d("aaaa", "tag:$accessToken")
        return prefRepository.getPref("token")?.let {
            "Bearer $it"
        } ?: ""
    }

    fun getOriginalToken(): String? {
        //Log.d("aaaa", "tag:$accessToken")
        return prefRepository.getPref("token")
    }

    fun setToken(token: String) {
        prefRepository.setPref("token", token)
    }

    fun getRefAreaId(): List<Int> {
        return prefRepository.getPref("refAreaId")?.trimEnd()?.split(" ")?.map {
            it.toInt()
        } ?: emptyList()
    }

    fun setRefAreaId(refAreaId: List<Int>) {
        val builder = StringBuilder()
        refAreaId.forEach {
            builder.append("$it ")
        }
        Log.d("aaaaa", builder.toString())
        prefRepository.setPref("refAreaId", builder.toString())
    }
}