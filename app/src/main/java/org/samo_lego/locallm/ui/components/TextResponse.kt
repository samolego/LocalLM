package org.samo_lego.locallm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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
        Box(
            modifier = Modifier
                .background(Color.DarkGray),

            ) {
            Column {
                Icon(Icons.Filled.Face, contentDescription = "Bot")
            }
            Column {
                Text(message)
            }
        }
    }
}