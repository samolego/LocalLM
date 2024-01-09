package org.samo_lego.locallm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MessageBubble(message: String, isUser: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isUser) 32.dp else 0.dp,
                end = if (isUser) 0.dp else 32.dp,
            )
            .clip(
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 0.dp,
                    bottomEnd = if (isUser) 0.dp else 16.dp,
                ),
            )

            .background(if (isUser) Color.LightGray else Color.DarkGray),
    ) {
        Text(
            text = message,
            textAlign = if (isUser) TextAlign.End else TextAlign.Start,
            modifier = Modifier.padding(
                vertical = 8.dp,
                horizontal = 16.dp,
            )
        )
    }
}

@Preview
@Composable
fun UserMessageBubblePreview() {
    MessageBubble(message = "Hello, I'm user", isUser = true)
}

@Preview
@Composable
fun BotMessageBubblePreview() {
    MessageBubble(message = "Bot here", isUser = false)
}