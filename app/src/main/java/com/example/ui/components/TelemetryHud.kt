package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TelemetryData
import com.example.ui.theme.MacaronMint
import com.example.ui.theme.MacaronPink
import com.example.ui.theme.TextDarkCocoa
import com.example.ui.theme.TextMutedCocoa

@Composable
fun TelemetryHud(
    telemetry: TelemetryData,
    modifier: Modifier = Modifier
) {
    SkeuoCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        elevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Latency
            TelemetryItem(
                label = "超低延迟",
                value = "${telemetry.latencyMs}ms",
                highlightColor = if (telemetry.latencyMs < 50) MacaronMint else MacaronPink,
                modifier = Modifier.weight(1f)
            )

            // FPS
            TelemetryItem(
                label = "同步帧率",
                value = "${telemetry.fps} FPS",
                highlightColor = Color(0xFF6C5CE7),
                modifier = Modifier.weight(1f)
            )

            // Bitrate
            TelemetryItem(
                label = "瞬时码率",
                value = "${telemetry.bitrateMbps}M",
                highlightColor = Color(0xFF0984E3),
                modifier = Modifier.weight(1f)
            )

            // Resolution
            TelemetryItem(
                label = "大屏规格",
                value = if (telemetry.width > 0) "${telemetry.width}p" else "自适应",
                highlightColor = TextDarkCocoa,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TelemetryItem(
    label: String,
    value: String,
    highlightColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextMutedCocoa,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(highlightColor.copy(alpha = 0.12f))
                .border(1.dp, highlightColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = value,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = highlightColor,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
