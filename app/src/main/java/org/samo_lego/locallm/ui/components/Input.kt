package org.samo_lego.locallm.ui.components

import android.speech.SpeechRecognizer
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    var rms by rememberSaveable {
        mutableStateOf(0f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        // Transparent background
    ) {
        if (sttEngine.isAvailable()) {
            IconButton(
                colors = IconButtonDefaults.filledIconButtonColors(),
                modifier = Modifier
                    .padding(8.dp)
                    .size(
                        animateDpAsState(
                            ((sqrt(abs(rms)) + 1) * 64).dp,
                            label = "MicIcon"
                        ).value
                    ),
                onClick = {
                    // Launch speech recognition
                    sttEngine.startListening(
                        onResults = { results ->
                            if (results != null) {
                                val data =
                                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                if (data != null) {
                                    // Merge all recognized words into one string
                                    val recognized = data.joinToString("")

                                    // Send recognized text
                                    if (appSettings.getBool(SettingsKeys.AUTO_SEND, true)) {
                                        onTextSend(recognized)
                                    } else {
                                        text = recognized
                                    }
                                }
                            }
                        },
                        onEndOfSpeech = {
                            rms = 0f
                        },
                        onRmsChanged = {
                            rms = it
                        }
                    )
                },
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Mic,
                        modifier = Modifier.size(32.dp),
                        contentDescription = "Localized description",
                    )
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

