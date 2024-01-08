package org.samo_lego.locallm.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class TextResponse(val isBot: Boolean, val message: String) {

    companion object {
        @Composable
        fun User(message: String) {
            Text("User: $message")
        }

        @Composable
        fun Bot(message: String) {
            Text("Bot: $message")
        }
    }
}