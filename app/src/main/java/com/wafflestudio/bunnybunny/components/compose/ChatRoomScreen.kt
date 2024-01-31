package com.wafflestudio.bunnybunny.components.compose

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wafflestudio.bunnybunny.viewModel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.WebSocket

@Composable
fun ChatRoomScreen(viewModel: ChatViewModel, channelId: Long) {
    var cur by remember { mutableStateOf(255) }
    val coroutineScope = rememberCoroutineScope()
    val messages by viewModel.messagesStateFlow.collectAsState()
    var websocket by remember { mutableStateOf<WebSocket?>(null) }

    val messageList by viewModel.latestMessage.collectAsState()

    DisposableEffect(Unit) {
        // Cleanup WebSocket on dispose
        onDispose {
            websocket?.close(1000, "ChatInputBox disposed")
        }
    }

    LaunchedEffect(channelId) {
        try {
            coroutineScope.launch {
                viewModel.connectToChatRoom(channelId)
                Log.d("CRS", "${websocket==null}")
                viewModel.sendRecentMessagesFormattedStringRequest(255)
                delay(500)
                viewModel.sendRecentMessagesFormattedStringRequest(255)
            }
        } catch (e: Exception) {
            Log.d("CHAT", e.message!!)
        }
    }


    Column (modifier = Modifier.fillMaxSize()) {
        LazyColumn ( reverseLayout = true,
            modifier = Modifier
                .fillMaxHeight(0.9f) // Set the LazyColumn height to 80% of the screen height
                .fillMaxWidth()
                .padding(8.dp, 8.dp)
            ,verticalArrangement = Arrangement.Top) {
                    items(messages) { message ->
                        Column {
                            ChatBubble(modifier = Modifier, message = message)
                            Spacer(modifier = Modifier.size(8.dp))
                        }

                    }
            }

        ChatInputBox(modifier = Modifier, viewModel)
    }








}