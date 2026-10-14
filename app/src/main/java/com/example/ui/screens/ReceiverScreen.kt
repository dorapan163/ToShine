package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioMode
import com.example.model.CastStatus
import com.example.ui.AppMode
import com.example.ui.ReceiverViewModel
import com.example.ui.components.CandyBadge
import com.example.ui.components.LedIndicator
import com.example.ui.components.SkeuoButton
import com.example.ui.components.SkeuoCard
import com.example.ui.components.TelemetryHud
import com.example.ui.components.TvFrameContainer
import com.example.ui.dialogs.ConnectionRequestDialog
import com.example.ui.dialogs.HdmiSettingsDialog
import com.example.ui.dialogs.PairingHelpDialog
import com.example.ui.theme.MacaronButter
import com.example.ui.theme.MacaronMint
import com.example.ui.theme.MacaronPink
import com.example.ui.theme.MacaronSky
import com.example.ui.theme.SkeuoBezelBg
import com.example.ui.theme.SkeuoScreenBg
import com.example.ui.theme.TextDarkCocoa
import com.example.ui.theme.TextMutedCocoa

@Composable
fun ReceiverScreen(
    viewModel: ReceiverViewModel,
    modifier: Modifier = Modifier
) {
    val appMode by viewModel.appMode.collectAsState()
    val status by viewModel.status.collectAsState()
    val frame by viewModel.latestFrame.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val client by viewModel.connectedClient.collectAsState()
    val hdmiState by viewModel.hdmiState.collectAsState()
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val aspectRatioMode by viewModel.aspectRatioMode.collectAsState()
    val orientationConfig by viewModel.orientationConfig.collectAsState()
    val isFullscreen by viewModel.isFullscreen.collectAsState()
    val showHdmiDialog by viewModel.showHdmiDialog.collectAsState()
    val showPairingDialog by viewModel.showPairingDialog.collectAsState()
    val pendingRequest by viewModel.pendingConnectionRequest.collectAsState()

    if (isFullscreen) {
        BackHandler { viewModel.setFullscreen(false) }

        // Fullscreen edge-to-edge projection mode
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SkeuoScreenBg),
            contentAlignment = Alignment.Center
        ) {
            if (frame?.bitmap != null && !frame!!.bitmap.isRecycled) {
                val contentScale = when (aspectRatioMode) {
                    AspectRatioMode.FIT -> ContentScale.Fit
                    AspectRatioMode.FILL -> ContentScale.FillBounds
                    AspectRatioMode.ORIGINAL_16_9 -> ContentScale.Inside
                }

                Image(
                    bitmap = frame!!.bitmap.asImageBitmap(),
                    contentDescription = "全屏同屏画面",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = orientationConfig.rotation.degrees.toFloat()
                            scaleX = if (orientationConfig.isFlippedHorizontally) -1f else 1f
                            scaleY = if (orientationConfig.isFlippedVertically) -1f else 1f
                        },
                    contentScale = contentScale
                )
            } else {
                Text(
                    text = "等待画面接入中…",
                    color = Color.White,
                    fontSize = 15.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Floating control pill
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(14.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x99000000))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${telemetry.fps} FPS | ${telemetry.latencyMs}ms",
                    color = MacaronMint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
                IconButton(
                    onClick = { viewModel.setFullscreen(false) },
                    modifier = Modifier
                        .testTag("exit_fullscreen_button")
                        .size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloseFullscreen,
                        contentDescription = "退出全屏",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SkeuoBezelBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Switcher Banner (Receiver vs Native Sender)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFFFFDDE4), RoundedCornerShape(18.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AppMode.entries.forEach { mode ->
                        val isSelected = mode == appMode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(15.dp))
                                .background(if (isSelected) MacaronPink else Color.Transparent)
                                .clickable { viewModel.setAppMode(mode) }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                color = if (isSelected) Color.White else TextDarkCocoa,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            if (appMode == AppMode.SENDER) {
                SenderScreen(
                    streamClient = viewModel.streamClient,
                    defaultTargetIp = deviceInfo.ip,
                    onBackToReceiver = { viewModel.setAppMode(AppMode.RECEIVER) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Top Bar
                        TopStatusHeader(
                            ssid = deviceInfo.ssid,
                            status = status,
                            hdmiState = hdmiState,
                            onRefresh = { viewModel.refreshNetworkInfo() },
                            onHdmiClick = { viewModel.setShowHdmiDialog(true) },
                            onHelpClick = { viewModel.setShowPairingDialog(true) }
                        )

                        // TV Monitor Frame displaying Mirrored Feed or Standby QR Code
                        TvFrameContainer(
                            frame = frame,
                            status = status,
                            pairingUrl = deviceInfo.webPairingUrl,
                            aspectRatioMode = aspectRatioMode,
                            orientationConfig = orientationConfig,
                            onRotateClick = { viewModel.cycleRotation() },
                            onInvertClick = { viewModel.toggleFlip180() },
                            onAspectClick = { viewModel.toggleAspectMode() },
                            onFullscreenClick = { viewModel.toggleFullscreen() },
                            onSnapshotClick = { viewModel.captureSnapshot() },
                            onStartTestStream = { viewModel.startSelfTestStream() }
                        )

                        // Low-latency Telemetry HUD
                        TelemetryHud(telemetry = telemetry)

                        // Connected Device Card (or Standby Notice)
                        ConnectedDeviceCard(
                            client = client,
                            status = status,
                            deviceInfo = deviceInfo,
                            onStartTest = { viewModel.startSelfTestStream() },
                            onStopTest = { viewModel.stopSelfTestStream() }
                        )

                        // HDMI Quick Action Dock
                        HdmiActionDock(
                            hdmiState = hdmiState,
                            onOpenHdmiSettings = { viewModel.setShowHdmiDialog(true) },
                            onTogglePresentation = { viewModel.toggleHdmiPresentation() }
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    // Connection Approval Dialog
    pendingRequest?.let { req ->
        ConnectionRequestDialog(
            request = req,
            onAccept = { viewModel.acceptConnectionRequest() },
            onReject = { viewModel.rejectConnectionRequest() }
        )
    }

    // Dialogs
    if (showHdmiDialog) {
        HdmiSettingsDialog(
            hdmiState = hdmiState,
            aspectRatioMode = aspectRatioMode,
            onAspectChange = { viewModel.setAspectMode(it) },
            onTogglePresentation = { viewModel.toggleHdmiPresentation() },
            onDismiss = { viewModel.setShowHdmiDialog(false) }
        )
    }

    if (showPairingDialog) {
        PairingHelpDialog(
            deviceInfo = deviceInfo,
            onDismiss = { viewModel.setShowPairingDialog(false) }
        )
    }
}

@Composable
private fun TopStatusHeader(
    ssid: String,
    status: CastStatus,
    hdmiState: com.example.model.HdmiOutputState,
    onRefresh: () -> Unit,
    onHdmiClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    SkeuoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: App Title & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🐰 糖心同屏",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MacaronPink,
                        maxLines = 1,
                        softWrap = false
                    )
                    LedIndicator(
                        isActive = status == CastStatus.STREAMING || status == CastStatus.WAITING,
                        label = if (status == CastStatus.STREAMING) "LIVE" else "READY"
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onHelpClick,
                        modifier = Modifier
                            .testTag("help_button")
                            .size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "帮助",
                            tint = MacaronPink,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .testTag("refresh_network_button")
                            .size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = TextDarkCocoa,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Row 2: Wi-Fi status pill & HDMI status pill (adaptive weights, single-line)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wi-Fi Pill
                Row(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF5F7))
                        .border(1.dp, Color(0xFFFFDDE4), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = MacaronMint,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = ssid,
                        fontSize = 11.sp,
                        color = TextMutedCocoa,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // HDMI Pill
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("hdmi_status_badge")
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (hdmiState.isConnected) MacaronMint.copy(alpha = 0.15f) else Color(0xFFF3EDEE))
                        .border(
                            1.dp,
                            if (hdmiState.isConnected) MacaronMint else Color(0xFFD8CCD0),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onHdmiClick() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = "HDMI 状态",
                            tint = if (hdmiState.isConnected) MacaronMint else TextMutedCocoa,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (hdmiState.isConnected) "HDMI 就绪" else "HDMI 待机",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hdmiState.isConnected) MacaronMint else TextMutedCocoa,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectedDeviceCard(
    client: com.example.model.ClientInfo?,
    status: CastStatus,
    deviceInfo: com.example.model.DeviceInfo,
    onStartTest: () -> Unit,
    onStopTest: () -> Unit
) {
    SkeuoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MacaronPink.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = MacaronPink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = if (client != null) "已连接设备" else "发送端状态",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkCocoa,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                CandyBadge(
                    text = status.label,
                    backgroundColor = if (status == CastStatus.STREAMING) MacaronMint else MacaronButter
                )
            }

            if (client != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF5F7))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = client.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkCocoa,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${client.platform} • ${client.ip}",
                            fontSize = 10.sp,
                            color = TextMutedCocoa,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (client.ip == "127.0.0.1") {
                        SkeuoButton(
                            onClick = onStopTest,
                            backgroundColor = Color(0xFFFF6B6B),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "停止自检",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF9FA))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "等待另一台手机扫码或连接",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDarkCocoa,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "浏览器访问 ${deviceInfo.webPairingUrl}",
                            fontSize = 10.sp,
                            color = TextMutedCocoa,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    SkeuoButton(
                        onClick = onStartTest,
                        backgroundColor = MacaronMint,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "本地测试",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HdmiActionDock(
    hdmiState: com.example.model.HdmiOutputState,
    onOpenHdmiSettings: () -> Unit,
    onTogglePresentation: () -> Unit
) {
    SkeuoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (hdmiState.isConnected) Color(0xFFE8F8F5) else Color.White,
        shape = RoundedCornerShape(20.dp),
        elevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (hdmiState.isConnected) MacaronMint else MacaronSky.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = if (hdmiState.isConnected) Color.White else MacaronSky,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "Type-C HDMI 大屏输出",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkCocoa,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = if (hdmiState.isConnected) {
                            "${hdmiState.displayName} (${hdmiState.resolution})"
                        } else {
                            "接入 Type-C 转接线自动点亮大屏"
                        },
                        fontSize = 10.sp,
                        color = TextMutedCocoa,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            SkeuoButton(
                onClick = onOpenHdmiSettings,
                backgroundColor = if (hdmiState.isConnected) MacaronMint else MacaronPink,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Text(
                    text = if (hdmiState.isConnected) "配置" else "设置",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
