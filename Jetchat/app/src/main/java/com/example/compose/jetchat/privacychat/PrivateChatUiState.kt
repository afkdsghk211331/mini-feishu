package com.example.compose.jetchat.privacychat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.toMutableStateList
import com.example.compose.jetchat.conversation.Message

@Immutable
class PrivateChatUiState(
    val contactName: String,
    val contactAvatar: Int,
    val isOnline: Boolean,
    initialMessages: List<Message>
) {
    private val _messages: MutableList<Message> = initialMessages.toMutableStateList()
    val messages: List<Message> = _messages
    fun addMessage(msg: Message) {
        _messages.add(0, msg)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun markMessagesAsRead() {
        _messages.replaceAll {
            if (!it.isFromMe()) it.copy() else it
        }
    }
}

fun Message.isFromMe(): Boolean = author == "me"
val Message.isRead: Boolean
    get() = this.timestamp.endsWith("✓")