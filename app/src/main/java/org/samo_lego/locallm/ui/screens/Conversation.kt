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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.samo_lego.locallm.data.SettingsKeys
import org.samo_lego.locallm.data.appSettings
import org.samo_lego.locallm.data.defaultSystem
import org.samo_lego.locallm.lmloader.LMHolder
import org.samo_lego.locallm.ui.components.Input
import org.samo_lego.locallm.ui.components.message.BotMessage
import org.samo_lego.locallm.ui.components.message.TextResponse
import org.samo_lego.locallm.ui.components.message.UserMessage
import org.samo_lego.locallm.util.ChatMLUtil
import org.samo_lego.locallm.voice.tts
import kotlin.math.max


@Composable
fun Conversation(
    currentMessage: MutableState<String>,
    messages: SnapshotStateList<TextResponse>,
    onMessageGenerated: () -> Unit,
) {
    val ttScope = rememberCoroutineScope()
    val contextScope = rememberCoroutineScope()
    var backticks = 0
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberLazyListState()

    var allowGenerating by remember { mutableStateOf(false) }
    var botResponse by remember {
        mutableStateOf(BotMessage())
    }

    LaunchedEffect(messages.size) {
        scrollState.animateScrollToItem(max(messages.size - 1, 0))
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
                        key(message.getText()) {
                            message.Render()
                        }
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

                            // Get context
                            contextScope.launch {
                                // Check if message is first
                                val context = if (messages.isEmpty()) {
                                    val current = LMHolder.currentModel()
                                    if (current != null) {
                                        ChatMLUtil.toChatML(current.systemPrompt, text)
                                    } else {
                                        ChatMLUtil.toChatML(defaultSystem, text)
                                    }
                                } else {
                                    ChatMLUtil.addUserMessage(currentMessage.value, text)
                                }

                                // Add new text response to view
                                messages.add(UserMessage(text))

                                // Create new bot response
                                botResponse = BotMessage()
                                messages.add(botResponse)

                                // Run model to generate response
                                tts.reset()
                                allowGenerating = true
                                LMHolder.suggest(
                                    context,
                                    onSuggestion = { suggestion ->
                                        if (!allowGenerating) {
                                            return@suggest false
                                        }
                                        Log.v("LocalLM", "Suggestion: $suggestion")
                                        if (suggestion.isEmpty()) {
                                            currentMessage.value += ChatMLUtil.im_end
                                            onEndSuggestions(
                                                botResponse,
                                                ttScope,
                                                onMessageGenerated,
                                            )
                                            allowGenerating = false

                                            return@suggest false
                                        } else {
                                            currentMessage.value += suggestion
                                            botResponse.appendToken(suggestion)

                                            if (botResponse.isComplete()) {
                                                onEndSuggestions(
                                                    botResponse,
                                                    ttScope,
                                                    onMessageGenerated,
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
                                        if (messages.last().getText().isEmpty()) {
                                            // Remove empty response
                                            messages.remove(botResponse)
                                        }

                                        onEndSuggestions(
                                            botResponse,
                                            ttScope,
                                            onMessageGenerated,
                                        )
                                        allowGenerating = false
                                    })
                            }
                        }
                    },
                    onForceStopGeneration = {
                        allowGenerating = false
                        tts.reset()
                        botResponse.appendToken(" ... ")
                    }

                )
            }
        }
    }
}

private fun onEndSuggestions(
    botResponse: BotMessage,
    ttScope: CoroutineScope,
    onMsgGenerated: () -> Unit,
) {
    // Ended stream
    botResponse.complete()
    // Finish the sentence
    tts.finishSentence(ttScope)
    onMsgGenerated()
}