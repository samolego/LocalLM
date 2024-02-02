package org.samo_lego.locallm.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import org.samo_lego.locallm.config.SettingsKeys
import org.samo_lego.locallm.config.appSettings
import org.samo_lego.locallm.lmloader.LMHolder
import org.samo_lego.locallm.ui.components.BotMessage
import org.samo_lego.locallm.ui.components.Input
import org.samo_lego.locallm.ui.components.TextResponse
import org.samo_lego.locallm.ui.components.UserMessage
import org.samo_lego.locallm.util.ChatMLUtil.Companion.im_end
import org.samo_lego.locallm.voice.tts

private val messages: MutableList<TextResponse> = mutableStateListOf()

@Preview(showBackground = true)
@Composable
fun Conversation() {
    val ttScope = rememberCoroutineScope()

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
        Input(
            onTextSend = {
                val text = it.trim()

                if (text.isNotEmpty()) {
                    // Add new text response to view
                    messages.add(UserMessage(text))

                    var botResponse = BotMessage()
                    var botTokens = botResponse.tokens.value
                    messages.add(botResponse)

                    // Run model to generate response
                    tts.reset()
                    LMHolder.suggest(text, onSuggestion = { suggestion ->
                        Log.v("LocalLM", "Suggestion: $suggestion")
                        if (suggestion.isEmpty()) {
                            // Ended stream
                            // Check for ChatML end of sentence
                            if (botTokens.contains(im_end)) {
                                botTokens.removeRange(
                                    botTokens.indexOf(im_end),
                                    botTokens.length - 1
                                )
                            }
                            // Finish the sentence
                            tts.finishSentence(ttScope)
                        } else {
                            if (suggestion.last() == '\n' && botTokens.endsWith('\n')) {
                                botResponse = BotMessage()
                                botTokens = botResponse.tokens.value
                                messages.add(botResponse)
                            } else if (suggestion != "\n") {
                                botResponse.appendToken(suggestion)

                                // Trigger update
                                //botResponse.tokens.value = botTokens
                            }

                            if (appSettings.getBool(SettingsKeys.USE_TTS, true)) {
                                tts.addWord(ttScope, suggestion)
                            }
                        }
                    })
                }
            },
        )
    }
}