package org.samo_lego.locallm.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

interface TextResponse {

    @Composable
    fun Render()
}

class UserMessage(val message: String) : TextResponse {
    @Composable
    override fun Render() {
        MessageBubble(message = message, isUser = true)
    }
}

class BotMessage : TextResponse {

    val tokens = mutableStateOf("")

    fun appendToken(token: String) {
        tokens.value += token
    }

    @Composable
    override fun Render() {
        val message by remember { tokens }
        MessageBubble(message = message, isUser = false)
    }
}
