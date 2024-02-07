package org.samo_lego.locallm.ui.components

import android.speech.SpeechRecognizer
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.samo_lego.locallm.config.SettingsKeys
import org.samo_lego.locallm.config.appSettings
import org.samo_lego.locallm.voice.STTEngine
import kotlin.math.abs
import kotlin.math.sqrt

@Composable
fun Input(
    onTextSend: (String) -> Unit = {},
    onForceStopGeneration: () -> Unit = {},
    isGenerating: Boolean
) {
    val sttEngine = STTEngine(LocalContext.current)

    var text by rememberSaveable {
        mutableStateOf("")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (sttEngine.isAvailable()) {
            MicInput(
                speechToText = sttEngine,
            ) { recognized ->
                if (appSettings.getBool(SettingsKeys.AUTO_SEND, true)) {
                    onTextSend(recognized)
                } else {
                    text = recognized
                }
            }
        }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = { text = it },
            trailingIcon = {
                IconButton(
                    colors = IconButtonDefaults.filledIconButtonColors(),
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        if (isGenerating) {
                            onForceStopGeneration()
                        }

                        if (text.isNotEmpty()) {
                            onTextSend(text)
                            text = ""
                        }
                    },
                ) {
                    Icon(
                        if (isGenerating) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Button",
                    )
                }
            },
        )
    }
}

@Composable
fun MicInput(speechToText: STTEngine, onComplete: (String) -> Unit = {}) {
    // Last 5 rms values
    var rms by rememberSaveable {
        mutableFloatStateOf(0f)
    }

    var isRecognizing by rememberSaveable {
        mutableStateOf(false)
    }

    var modifier = Modifier.offset(
        y = animateDpAsState(
            targetValue = if (isRecognizing) {
                // Middle of the screen
                -LocalConfiguration.current.screenHeightDp.dp / 2 + 196.dp
            } else {
                // Bottom of the screen
                0.dp
            },
            label = "MicIconOffset"
        ).value
    )

    if (isRecognizing) {
        modifier = modifier.requiredHeight(256.dp)
    }


    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        IconButton(
            colors = IconButtonDefaults.filledIconButtonColors(),
            modifier = Modifier
                .padding(8.dp)
                .size(
                    animateDpAsState(
                        if (isRecognizing) {
                            (rms * 64).dp
                        } else {
                            64.dp
                        },
                        label = "MicIcon"
                    ).value
                ),
            onClick = {
                // Launch speech recognition
                speechToText.startListening(
                    onResults = { results ->
                        if (results != null) {
                            val data =
                                results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (data != null) {
                                // Merge all recognized words into one string
                                val recognized = data.joinToString("")

                                // Send recognized text
                                onComplete(recognized)
                            }
                        }
                    },
                    onError = {
                        rms = 0f
                        isRecognizing = false
                    },
                    onReadyForSpeech = {
                        isRecognizing = true
                    },
                    onEndOfSpeech = {
                        rms = 0f
                        isRecognizing = false
                    },
                    onRmsChanged = {
                        rms = 0.25f * rms + 0.75f * sqrt(abs(it))
                    }
                )
            },
        ) {
            Icon(
                Icons.Default.Mic,
                modifier = Modifier.size(32.dp),
                contentDescription = "Localized description",
            )
        }
    }
}

@Preview
@Composable
fun InputPreview() {
    Input(isGenerating = false)
}

@Preview
@Composable
fun InputPreviewGenerating() {
    Input(isGenerating = true)
}

