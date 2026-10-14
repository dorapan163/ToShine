package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MacaronMint
import com.example.ui.theme.MacaronPink
import com.example.ui.theme.SkeuoBorder
import com.example.ui.theme.SkeuoShadowDark
import com.example.ui.theme.SkeuoShadowLight
import com.example.ui.theme.TextDarkCocoa
import kotlin.math.roundToInt

@Composable
fun SkeuoCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    backgroundColor: Color = Color.White,
    borderColor: Color = SkeuoBorder,
    elevation: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = SkeuoShadowDark,
                spotColor = SkeuoShadowDark
            )
            .clip(shape)
            .background(backgroundColor)
            .border(2.dp, borderColor, shape)
            .drawBehind {
                // Subtle top-left tactile light reflection
                drawLine(
                    color = SkeuoShadowLight.copy(alpha = 0.8f),
                    start = Offset(0f, 4f),
                    end = Offset(size.width, 4f),
                    strokeWidth = 3f
                )
            },
        content = content
    )
}

@Composable
fun SkeuoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "skeuo_button",
    backgroundColor: Color = MacaronPink,
    pressedColor: Color = MacaronPink.copy(alpha = 0.85f),
    shape: Shape = RoundedCornerShape(22.dp),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY by animateFloatAsState(
        targetValue = if (isPressed) 4f else 0f,
        animationSpec = spring(),
        label = "buttonOffset"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = shape,
                ambientColor = SkeuoShadowDark,
                spotColor = SkeuoShadowDark
            )
            .clip(shape)
            .background(if (isPressed) pressedColor else backgroundColor)
            .border(2.dp, Color.White.copy(alpha = 0.6f), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                performHapticClick(context)
                onClick()
            }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun LedIndicator(
    isActive: Boolean,
    label: String,
    modifier: Modifier = Modifier,
    activeColor: Color = MacaronMint,
    inactiveColor: Color = Color(0xFFD4C8CA)
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .shadow(
                    elevation = if (isActive) 6.dp else 1.dp,
                    shape = CircleShape,
                    ambientColor = if (isActive) activeColor else Color.Transparent,
                    spotColor = if (isActive) activeColor else Color.Transparent
                )
                .clip(CircleShape)
                .background(
                    if (isActive) {
                        Brush.radialGradient(
                            listOf(Color.White, activeColor, activeColor.copy(alpha = 0.8f))
                        )
                    } else {
                        Brush.linearGradient(listOf(inactiveColor, inactiveColor.copy(alpha = 0.6f)))
                    }
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) TextDarkCocoa else Color(0xFFAAA0A2)
        )
    }
}

@Composable
fun CandyBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MacaronMint,
    textColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.5.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun performHapticClick(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(20)
        }
    } catch (_: Exception) {}
}
