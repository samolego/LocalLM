package org.samo_lego.locallm.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import org.samo_lego.locallm.ui.components.Input

private val messages: List<ViewModel> = mutableStateListOf()

@Preview(showBackground = true)
@Composable
fun Conversation() {
    Row {
        Column {
            messages.forEach { message ->
                message
            }
        }
        Column {
            Input(onTextSend = { text ->
                // Add new text reponse to view
            })
        }
    }
}