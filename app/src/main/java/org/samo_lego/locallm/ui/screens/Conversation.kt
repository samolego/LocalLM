package org.samo_lego.locallm.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.samo_lego.locallm.lmloader.LMHolder
import org.samo_lego.locallm.ui.components.BotMessage
import org.samo_lego.locallm.ui.components.Input
import org.samo_lego.locallm.ui.components.TextResponse
import org.samo_lego.locallm.ui.components.UserMessage

private val messages: MutableList<TextResponse> = mutableStateListOf()

@Preview(showBackground = true)
@Composable
fun Conversation() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Transparent) // Replace with your styling for message history
        ) {

            LazyColumn {
                items(messages) { message ->
                    Column {
                        message.Render()
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Input(
                onTextSend = {
                    val text = it.trim()

                    if (text.isNotEmpty()) {
                        // Add new text response to view
                        messages.add(UserMessage(text))


                        val botTokens = mutableStateListOf<String>()
                        val botResponse = BotMessage(botTokens)
                        messages.add(botResponse)

                        // Run model to generate response
                        LMHolder.suggest(text, onSuggestion = { suggestion ->
                            Log.v("LocalLM", "Suggestion: $suggestion")
                            botTokens.add(suggestion)
                        })
                    }
                },
            )
        }
    }
}