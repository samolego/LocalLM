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

    fun getLastSentence(): String {
        var lastSentence = ""
        for (token in messageTokens.reversed()) {
            if (token.contains(".") || token.contains("?") || token.contains("!")) {
                break
            }
            lastSentence = token + lastSentence
        }
        return lastSentence
    }

    @Composable
    override fun Render() {
        MessageBubble(message = message, isUser = false)
    }
}