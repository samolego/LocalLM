package org.samo_lego.locallm.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import org.samo_lego.locallm.ui.components.Input
import org.samo_lego.locallm.ui.components.TextResponse

private val messages: MutableList<TextResponse> = mutableStateListOf()

@Composable
fun Conversation() {
    Row {
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            items(messages) { message ->
                Column {
                    if (message.isBot) {
                        TextResponse.Bot(message.message)
                    } else {
                        TextResponse.User(message.message)
                    }
                }
            }
        }
        Column {
            Input(onTextSend = {
                val text = it.trim()

                if (text.isNotEmpty()) {
                    // Add new text reponse to view
                    messages.add(TextResponse(false, text))
                }
            })
        }
    }
}