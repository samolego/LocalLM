package org.samo_lego.locallm.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.samo_lego.locallm.data.SettingsKeys
import org.samo_lego.locallm.data.appSettings
import org.samo_lego.locallm.lmloader.LMHolder
import org.samo_lego.locallm.ui.components.Input
import org.samo_lego.locallm.ui.components.message.BotMessage
import org.samo_lego.locallm.ui.components.message.TextResponse
import org.samo_lego.locallm.ui.components.message.UserMessage
import org.samo_lego.locallm.voice.tts

private val messages: MutableList<TextResponse> = mutableStateListOf()

@Preview(showBackground = true)
@Composable
fun Conversation() {
    val ttScope = rememberCoroutineScope()
    var backticks = 0
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberLazyListState()

    var allowGenerating by remember { mutableStateOf(false) }
    var botResponse by remember {
        mutableStateOf(BotMessage())
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                state = scrollState,
                contentPadding = PaddingValues(
                    bottom = 128.dp,
                    top = 8.dp,
                    start = 8.dp,
                    end = 8.dp
                ),
            ) {
                items(messages) { message ->
                    Column {
                        message.Render()
                    }
                }
            }

            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Input(
                    isGenerating = allowGenerating,
                    onTextSend = { str ->
                        val txt = str.trim()

                        if (txt.isNotEmpty()) {
                            // Hide keyboard
                            keyboardController?.hide()

                            // Double each \n for markdown
                            val text = txt.replace("\n", "\n\n")


                            // Add new text response to view
                            messages.add(UserMessage(text))

                            // Create new bot response
                            botResponse = BotMessage()
                            messages.add(botResponse)

                            // Run model to generate response
                            tts.reset()
                            allowGenerating = true
                            LMHolder.suggest(text, onSuggestion = { suggestion ->
                                if (!allowGenerating) {
                                    return@suggest false
                                }
                                Log.v("LocalLM", "Suggestion: $suggestion")
                                if (suggestion.isEmpty()) {
                                    onEndSuggestions(
                                        botResponse,
                                        ttScope
                                    )
                                    allowGenerating = false

                                    return@suggest false
                                } else {
                                    botResponse.appendToken(suggestion)

                                    if (botResponse.isComplete()) {
                                        onEndSuggestions(
                                            botResponse,
                                            ttScope
                                        )
                                        allowGenerating = false
                                        return@suggest false
                                    }

                                    backticks += suggestion.count { it == '`' }

                                    if (botResponse.tokens.value.endsWith("\n\n") && backticks % 2 == 0) {
                                        // New bot message bubble
                                        botResponse = BotMessage()
                                        messages.add(botResponse)
                                    }

                                    if (appSettings.getBool(SettingsKeys.USE_TTS, true)) {
                                        tts.addWord(ttScope, suggestion)
                                    }

                                    // Scroll to bottom
                                    ttScope.launch {
                                        scrollState.animateScrollToItem(messages.size - 1)
                                    }
                                }

                                return@suggest true
                            },
                                onEnd = {
                                    onEndSuggestions(botResponse, ttScope)
                                    allowGenerating = false
                                })
                        }
                    },
                    onForceStopGeneration = {
                        allowGenerating = false
                        tts.reset()
                        botResponse.appendToken(" ... [-- Stopped --]")
                    }
                )
            }
        }
    }
}

private fun onEndSuggestions(botResponse: BotMessage, ttScope: CoroutineScope) {
    // Ended stream
    botResponse.complete()
    // Finish the sentence
    tts.finishSentence(ttScope)
}