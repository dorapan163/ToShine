package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioMode
import com.example.model.HdmiOutputState
import com.example.ui.components.CandyBadge
import com.example.ui.components.SkeuoButton
import com.example.ui.components.SkeuoCard
import com.example.ui.theme.MacaronButter
import com.example.ui.theme.MacaronMint
import com.example.ui.theme.MacaronPink
import com.example.ui.theme.MacaronSky
import com.example.ui.theme.TextDarkCocoa
import com.example.ui.theme.TextMutedCocoa

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HdmiSettingsDialog(
    hdmiState: HdmiOutputState,
    aspectRatioMode: AspectRatioMode,
    onAspectChange: (AspectRatioMode) -> Unit,
    onTogglePresentation: () -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        SkeuoCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            backgroundColor = Color(0xFFFFF9FB),
            shape = RoundedCornerShape(28.dp),
            elevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MacaronSky.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = MacaronSky,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Type-C 转 HDMI 投屏设置",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkCocoa
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .testTag("close_hdmi_dialog")
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = TextMutedCocoa
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Connection Status Card
                SkeuoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (hdmiState.isConnected) Color(0xFFE8F8F5) else Color(0xFFFFF0F3),
                    borderColor = if (hdmiState.isConnected) MacaronMint else MacaronPink,
                    shape = RoundedCornerShape(18.dp),
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (hdmiState.isConnected) "HDMI 硬件已连接" else "未连接 HDMI 设备",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkCocoa
                            )
                            CandyBadge(
                                text = if (hdmiState.isConnected) "ACTIVE" else "STANDBY",
                                backgroundColor = if (hdmiState.isConnected) MacaronMint else MacaronPink
                            )
                        }

                        if (hdmiState.isConnected) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "显示设备: ${hdmiState.displayName}",
                                fontSize = 12.sp,
                                color = TextDarkCocoa
                            )
                            Text(
                                text = "输出规格: ${hdmiState.resolution} @ ${hdmiState.refreshRate.toInt()}Hz",
                                fontSize = 12.sp,
                                color = TextMutedCocoa
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "通过 Type-C 转接线/扩展坞接入电视、显示器或采集卡，系统将自动识别并点亮大屏。",
                                fontSize = 11.sp,
                                color = TextMutedCocoa,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presentation Toggle
                if (hdmiState.isConnected) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.5.dp, Color(0xFFFFDDE4), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "大屏独占镜像 (Presentation)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkCocoa
                            )
                            Text(
                                text = "大屏纯净全屏展示，手机保留控制面板",
                                fontSize = 11.sp,
                                color = TextMutedCocoa
                            )
                        }

                        Switch(
                            checked = hdmiState.isPresenting,
                            onCheckedChange = { onTogglePresentation() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MacaronMint
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Aspect Ratio Selector
                Text(
                    text = "画面比例模式",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDarkCocoa,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AspectRatioMode.entries.forEach { mode ->
                        val isSelected = mode == aspectRatioMode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) MacaronPink else Color.White)
                                .border(1.5.dp, if (isSelected) MacaronPink else Color(0xFFFFDDE4), RoundedCornerShape(14.dp))
                                .clickable { onAspectChange(mode) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextDarkCocoa
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                SkeuoButton(
                    onClick = onDismiss,
                    backgroundColor = MacaronMint,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "完成设置",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
