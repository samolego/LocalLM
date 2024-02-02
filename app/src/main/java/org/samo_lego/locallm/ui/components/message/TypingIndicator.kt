package org.samo_lego.locallm.ui.components.message

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun TypingIndicator() {
    val max = 8f
    val dotPos = rememberInfiniteTransition(label = "infTrans").animateFloat(
        initialValue = 0f,
        targetValue = max,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "animFloat"
    )

    Row(
        modifier = Modifier
            .height(24.dp)
            .padding(max.dp)
    ) {
        Dot(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .graphicsLayer(
                    translationY = dotPos.value,
                ),
        )
        Dot(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .graphicsLayer(
                    translationY = max - dotPos.value,
                ),

            )
        Dot(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .graphicsLayer(
                    translationY = dotPos.value,
                ),
        )
    }
}

@Composable
fun Dot(modifier: Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .background(color = Color.LightGray, shape = CircleShape)
            .clip(CircleShape)
    )
}
