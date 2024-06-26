package org.samo_lego.locallm.ui.components.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material3.RichText

@Composable
fun MessageBubble(message: String, isUser: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = if (isUser) {
            Alignment.End
        } else {
            Alignment.Start
        },
    ) {
        Box(
            modifier = Modifier
                .padding(
                    start = if (isUser) 32.dp else 0.dp,
                    end = if (isUser) 0.dp else 32.dp,
                    top = 4.dp,
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 16.dp,
                    ),
                )

                .background(if (isUser) Color.Gray else Color.DarkGray),
        ) {
            if (message.isEmpty()) {
                Box(modifier = Modifier.padding(8.dp)) {
                    TypingIndicator()
                }
            } else {
                RichText(
                    modifier = Modifier.padding(
                        vertical = 8.dp,
                        horizontal = 16.dp,
                    ),
                    style = RichTextStyle.Default.copy(
                    ),
                ) {
                    Markdown(content = message)
                }
            }
        }
    }
}

@Preview
@Composable
fun UserMessageBubblePreview() {
    MessageBubble(
        message = "Hello, I'm user. Lorem ipsum long text which goes on and on to test overflow ...",
        isUser = true
    )
}

@Preview
@Composable
fun BotMessageBubblePreview() {
    MessageBubble(message = "", isUser = false)
}

@Preview
@Composable
fun TwoMessagesPreview() {
    Column {
        MessageBubble(
            message = "Hello, I'm user. Lorem ipsum long text which goes on and on to test overflow ...",
            isUser = true
        )
        MessageBubble(
            message = "**Bot here**.\n\n* Lorem ipsum testing overflow.[How far can this text go?](https://google.com) Will it break?",
            isUser = false
        )
        MessageBubble(
            message = "Bot here. Lorem ipsum testing overflow. How far can this text go? Will it break?",
            isUser = false
        )
    }
}
