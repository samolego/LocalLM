package org.samo_lego.locallm.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun Input(
    isGenerating: Boolean,
    onTextSend: (String) -> Unit = {},
    onForceStopGeneration: () -> Unit = {}
) {
    var text by rememberSaveable {
        mutableStateOf("")
    }

    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = text,
        onValueChange = { text = it },
        trailingIcon = {
            IconButton(
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
                    if (isGenerating) Icons.Default.Close else Icons.Default.Send,
                    contentDescription = "Send",
                )
            }
        },
    )
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

