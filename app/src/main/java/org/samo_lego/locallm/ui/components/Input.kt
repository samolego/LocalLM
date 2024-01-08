package org.samo_lego.locallm.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun Input(onTextSend: (String) -> Unit = {}) {
    var text by rememberSaveable {
        mutableStateOf("")
    }
    TextField(
        value = text,
        onValueChange = { text = it },
        trailingIcon = {
            IconButton(
                onClick = {
                    onTextSend(text)
                    text = ""
                },
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "Send",
                )
            }
        },
    )
}
