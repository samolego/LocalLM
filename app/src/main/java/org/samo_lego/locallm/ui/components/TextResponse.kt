package org.samo_lego.locallm.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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

class BotMessage(private val messageTokens: StringBuilder) : TextResponse {

    @Composable
    override fun Render() {
        var displayedMessage by remember {
            mutableStateOf(messageTokens.toString())
        }

        LaunchedEffect(messageTokens) {
            displayedMessage = messageTokens.toString()
        }

        MessageBubble(message = displayedMessage, isUser = false)
    }
}
