package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IncomingConnectionRequest
import com.example.ui.components.CandyBadge
import com.example.ui.components.SkeuoButton
import com.example.ui.components.SkeuoCard
import com.example.ui.theme.MacaronButter
import com.example.ui.theme.MacaronMint
import com.example.ui.theme.MacaronPink
import com.example.ui.theme.TextDarkCocoa
import com.example.ui.theme.TextMutedCocoa

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionRequestDialog(
    request: IncomingConnectionRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onReject) {
        SkeuoCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            backgroundColor = Color(0xFFFFF9FB),
            shape = RoundedCornerShape(28.dp),
            elevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MacaronPink.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = MacaronPink,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = "🐰 收到新设备投屏连接请求",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDarkCocoa,
                    textAlign = TextAlign.Center
                )

                // Client Details Card
                SkeuoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "设备名称",
                                fontSize = 12.sp,
                                color = TextMutedCocoa
                            )
                            CandyBadge(
                                text = request.platform,
                                backgroundColor = MacaronMint
                            )
                        }
                        Text(
                            text = request.deviceName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkCocoa
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "IP 地址",
                                fontSize = 12.sp,
                                color = TextMutedCocoa
                            )
                            Text(
                                text = request.clientIp,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MacaronPink
                            )
                        }
                    }
                }

                Text(
                    text = "是否允许该设备同步画面至本接收端及 Type-C HDMI 外接显示屏？",
                    fontSize = 12.sp,
                    color = TextMutedCocoa,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                // Decision Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SkeuoButton(
                        onClick = onReject,
                        backgroundColor = Color(0xFFFFDDE4),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reject_connection_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = TextDarkCocoa,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "拒绝",
                                color = TextDarkCocoa,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    SkeuoButton(
                        onClick = onAccept,
                        backgroundColor = MacaronMint,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("accept_connection_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "同意连接",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
