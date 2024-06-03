package org.samo_lego.locallm.ui.components.message

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import org.samo_lego.locallm.util.ChatMLUtil

interface TextResponse {

    @Composable
    fun Render()
    fun getText(): String

    companion object {
        fun fromText(text: String, separator: String): MutableList<TextResponse> {
            val responses = mutableListOf<TextResponse>()
            // Cut out system prompt (till first user message)
            val conversation = text.substringAfter(ChatMLUtil.im_end)
            val messages = conversation.split(separator)
            var isUser: Boolean
            for (message in messages) {
                if (message.isEmpty()) continue
                isUser = if (message.startsWith("user")) {
                    true
                } else if (message.startsWith("assistant")) {
                    false
                } else continue
                val cutMessage = message.removePrefix("user").removePrefix("assistant")

                // Remove chatml suffix
                val finalMsg = if (cutMessage.contains(ChatMLUtil.im_end)) {
                    val end = cutMessage.indexOf(ChatMLUtil.im_end)
                    cutMessage.slice(0 until end)
                } else {
                    cutMessage
                }

                if (finalMsg.isEmpty()) continue

                responses.add(
                    if (isUser) UserMessage(finalMsg)
                    else BotMessage().apply {
                        appendToken(finalMsg)
                        complete()
                    }
                )
            }

            return responses
        }
    }
}

class UserMessage(val message: String) : TextResponse {
    @Composable
    override fun Render() {
        MessageBubble(message = message, isUser = true)
    }

    override fun getText(): String {
        return message
    }

    override fun toString(): String {
        return getText()
    }
}

class BotMessage : TextResponse {

    val tokens = mutableStateOf("")
    private var savedTokens = ""

    private fun removeRange(start: Int, end: Int) {
        tokens.value = tokens.value.removeRange(start, end)
    }

    fun appendToken(token: String) {
        // Check for ChatML end
        val sentence = tokens.value + savedTokens + token
        if (!ChatMLUtil.isPotentialEnd(sentence)) {
            tokens.value = sentence
            savedTokens = ""
        } else {
            savedTokens += token
        }
    }

    fun isComplete() = savedTokens.contains(ChatMLUtil.im_end)

    fun complete() {
        tokens.value += savedTokens
        // Remove chatml suffix
        if (tokens.value.contains(ChatMLUtil.im_end)) {
            removeRange(
                tokens.value.indexOf(ChatMLUtil.im_end),
                tokens.value.length
            )
        }
        savedTokens = ""
    }

    override fun toString(): String {
        return getText()
    }

    @Preview
    @Composable
    override fun Render() {
        val message by remember { tokens }
        MessageBubble(message = message, isUser = false)
    }

    override fun getText(): String {
        return tokens.value
    }

}
