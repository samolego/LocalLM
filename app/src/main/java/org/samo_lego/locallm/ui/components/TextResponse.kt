package org.samo_lego.locallm.ui.components

import androidx.compose.runtime.Composable

interface TextResponse {
    val message: String

    @Composable
    fun Render()
}

class UserMessage(override val message: String) : TextResponse {
    @Composable
    override fun Render() {
        MessageBubble(message = message, isUser = true)
    }
}

class BotMessage(private val messageTokens: MutableList<String>) : TextResponse {
    override val message: String
        get() = messageTokens.joinToString(separator = "") { it }

    @Composable
    override fun Render() {
        MessageBubble(message = message, isUser = false)
    }
}