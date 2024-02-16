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
}

class UserMessage(val message: String) : TextResponse {
    @Composable
    override fun Render() {
        MessageBubble(message = message, isUser = true)
    }
}

class BotMessage : TextResponse {

    val tokens = mutableStateOf("")
    private var savedTokens = ""

    fun removeRange(start: Int, end: Int) {
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

    @Preview
    @Composable
    override fun Render() {
        val message by remember { tokens }
        MessageBubble(message = message, isUser = false)
    }

}
