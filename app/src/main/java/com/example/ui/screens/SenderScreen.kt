package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Matrix
import android.graphics.Paint as AndroidPaint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.model.ClientApprovalState
import com.example.network.MimiStreamClient
import com.example.ui.components.CandyBadge
import com.example.ui.components.LedIndicator
import com.example.ui.components.SkeuoButton
import com.example.ui.components.SkeuoCard
import com.example.ui.dialogs.QrScannerDialog
import com.example.ui.theme.MacaronButter
import com.example.ui.theme.MacaronMint
import com.example.ui.theme.MacaronPink
import com.example.ui.theme.MacaronSky
import com.example.ui.theme.TextDarkCocoa
import com.example.ui.theme.TextMutedCocoa
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.sin

enum class SenderTab(val label: String) {
    DRAW("🎨 互动画板"),
    CAMERA("📷 原生相机"),
    TEST("🐰 极速自检")
}

data class DrawnLine(val start: Offset, val end: Offset, val color: Color, val strokeWidth: Float = 8f)

@Composable
fun SenderScreen(
    streamClient: MimiStreamClient,
    defaultTargetIp: String,
    onBackToReceiver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var targetIp by remember { mutableStateOf(defaultTargetIp) }
    var selectedTab by remember { mutableStateOf(SenderTab.DRAW) }
    var showScanner by remember { mutableStateOf(false) }

    val isConnected by streamClient.isConnected.collectAsState()
    val approvalState by streamClient.approvalState.collectAsState()
    val statusMessage by streamClient.statusMessage.collectAsState()
    val framesSent by streamClient.framesSent.collectAsState()
    val latencyMs by streamClient.latencyMs.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            Toast.makeText(context, "需要相机权限以实现原生同屏", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Target Receiver Connection Card
        SkeuoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White,
            shape = RoundedCornerShape(22.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📱 原生同屏发送端",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MacaronPink,
                            maxLines = 1,
                            softWrap = false
                        )
                        LedIndicator(
                            isActive = isConnected,
                            label = if (isConnected) "已连大屏" else "待连",
                            activeColor = MacaronMint
                        )
                    }

                    CandyBadge(
                        text = approvalState.label,
                        backgroundColor = when (approvalState) {
                            ClientApprovalState.APPROVED -> MacaronMint
                            ClientApprovalState.WAITING_APPROVAL -> MacaronButter
                            ClientApprovalState.REJECTED -> Color(0xFFFF6B6B)
                            else -> Color(0xFFD4C8CA)
                        }
                    )
                }

                if (statusMessage.isNotBlank()) {
                    Text(
                        text = statusMessage,
                        fontSize = 11.sp,
                        color = if (approvalState == ClientApprovalState.REJECTED) Color(0xFFFF4081) else TextMutedCocoa,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // IP input row with Scan button and Connect button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = targetIp,
                        onValueChange = { targetIp = it },
                        placeholder = { Text("接收端 IP (例: 192.168.1.5)", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sender_ip_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MacaronPink,
                            unfocusedBorderColor = Color(0xFFFFDDE4)
                        )
                    )

                    // Scan QR Button
                    SkeuoButton(
                        onClick = { showScanner = true },
                        backgroundColor = MacaronSky,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "扫码连接",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "扫码",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Connect/Disconnect Button
                    SkeuoButton(
                        onClick = {
                            if (isConnected || approvalState == ClientApprovalState.WAITING_APPROVAL) {
                                streamClient.disconnect()
                            } else {
                                streamClient.connect(targetInput = targetIp.trim())
                            }
                        },
                        backgroundColor = if (isConnected) Color(0xFFFF6B6B) else MacaronMint,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.LinkOff else Icons.Default.Link,
                            contentDescription = if (isConnected) "断开" else "连接",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isConnected) "断开" else "连接",
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

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(1.5.dp, Color(0xFFFFDDE4), RoundedCornerShape(18.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SenderTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(15.dp))
                        .background(if (isSelected) MacaronPink else Color.Transparent)
                        .clickable { selectedTab = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        color = if (isSelected) Color.White else TextDarkCocoa,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // Active Tab Pane
        when (selectedTab) {
            SenderTab.DRAW -> {
                NativeDoodlerPane(
                    isConnected = isConnected && approvalState == ClientApprovalState.APPROVED,
                    onCanvasUpdated = { bitmap ->
                        streamClient.sendBitmapFrame(bitmap)
                    }
                )
            }
            SenderTab.CAMERA -> {
                NativeCameraPane(
                    hasPermission = hasCameraPermission,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onFrameCaptured = { bitmap ->
                        if (isConnected && approvalState == ClientApprovalState.APPROVED) {
                            streamClient.sendBitmapFrame(bitmap)
                        }
                    }
                )
            }
            SenderTab.TEST -> {
                NativeTestStreamPane(
                    isConnected = isConnected && approvalState == ClientApprovalState.APPROVED,
                    onSendTestPattern = { bitmap ->
                        streamClient.sendBitmapFrame(bitmap)
                    }
                )
            }
        }

        // Telemetry status footer
        SkeuoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFFFFF9FA),
            shape = RoundedCornerShape(18.dp),
            elevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "已推送: $framesSent 帧",
                    fontSize = 11.sp,
                    color = TextMutedCocoa,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "编码延迟: ${latencyMs}ms",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MacaronMint,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }

    if (showScanner) {
        QrScannerDialog(
            onScanned = { scannedUrl ->
                showScanner = false
                val (host, port) = MimiStreamClient.sanitizeHostAndPort(scannedUrl)
                targetIp = if (port != 8080) "$host:$port" else host
                streamClient.connect(targetInput = targetIp)
            },
            onDismiss = { showScanner = false }
        )
    }
}

@Composable
private fun NativeDoodlerPane(
    isConnected: Boolean,
    onCanvasUpdated: (Bitmap) -> Unit
) {
    val lines = remember { mutableStateListOf<DrawnLine>() }
    var currentColor by remember { mutableStateOf(MacaronPink) }
    var isFlipped180 by remember { mutableStateOf(false) }

    // Backing bitmap buffer
    val backingWidth = 720
    val backingHeight = 540
    val backingBitmap = remember {
        Bitmap.createBitmap(backingWidth, backingHeight, Bitmap.Config.ARGB_8888).apply {
            eraseColor(AndroidColor.WHITE)
        }
    }
    val backingCanvas = remember { AndroidCanvas(backingBitmap) }
    val paint = remember {
        AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            strokeCap = AndroidPaint.Cap.ROUND
            strokeJoin = AndroidPaint.Join.ROUND
            strokeWidth = 14f
        }
    }

    SkeuoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "画板涂鸦实时同屏",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDarkCocoa,
                    maxLines = 1,
                    softWrap = false
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Flip 180 button (correct inverted receiver)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isFlipped180) MacaronPink else Color(0xFFF3EDEE))
                            .clickable {
                                isFlipped180 = !isFlipped180
                                // Send flipped frame
                                val matrix = Matrix()
                                if (isFlipped180) matrix.postRotate(180f)
                                val transformed = Bitmap.createBitmap(
                                    backingBitmap, 0, 0, backingWidth, backingHeight, matrix, true
                                )
                                onCanvasUpdated(transformed)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isFlipped180) "已翻转180°" else "翻转纠正",
                            fontSize = 10.sp,
                            color = if (isFlipped180) Color.White else TextDarkCocoa,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Clear button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFDDE4))
                            .clickable {
                                lines.clear()
                                backingBitmap.eraseColor(AndroidColor.WHITE)
                                onCanvasUpdated(backingBitmap)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "清屏",
                            fontSize = 10.sp,
                            color = TextDarkCocoa,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // Drawing Area with pixel-accurate coordinate scaling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(2.dp, Color(0xFFFFDDE4), RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        var currentPos = Offset.Zero
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPos = offset
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val nextPos = currentPos + dragAmount
                                lines.add(DrawnLine(currentPos, nextPos, currentColor))

                                // Accurately scale from Compose view size to backing bitmap
                                val scaleX = backingWidth.toFloat() / size.width.toFloat()
                                val scaleY = backingHeight.toFloat() / size.height.toFloat()

                                paint.color = currentColor.toArgb()
                                backingCanvas.drawLine(
                                    currentPos.x * scaleX,
                                    currentPos.y * scaleY,
                                    nextPos.x * scaleX,
                                    nextPos.y * scaleY,
                                    paint
                                )

                                currentPos = nextPos

                                // Send updated frame immediately
                                val matrix = Matrix()
                                if (isFlipped180) matrix.postRotate(180f)
                                val frameToSend = if (isFlipped180) {
                                    Bitmap.createBitmap(backingBitmap, 0, 0, backingWidth, backingHeight, matrix, true)
                                } else {
                                    backingBitmap
                                }
                                onCanvasUpdated(frameToSend)
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    lines.forEach { line ->
                        drawLine(
                            color = line.color,
                            start = line.start,
                            end = line.end,
                            strokeWidth = line.strokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }

            // Color palette
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(MacaronPink, MacaronMint, MacaronButter, MacaronSky, Color(0xFF4A3E3D)).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    if (currentColor == color) 3.dp else 1.dp,
                                    if (currentColor == color) Color.White else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { currentColor = color }
                        )
                    }
                }

                Text(
                    text = if (isConnected) "● 实时同步推流中" else "○ 需连接接收端同屏",
                    fontSize = 11.sp,
                    color = if (isConnected) MacaronMint else TextMutedCocoa,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun NativeCameraPane(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onFrameCaptured: (Bitmap) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var isFlipped180 by remember { mutableStateOf(false) }

    SkeuoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "原生相机硬件加速流",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDarkCocoa,
                    maxLines = 1,
                    softWrap = false
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { isFlipped180 = !isFlipped180 },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = "翻转纠正",
                            tint = if (isFlipped180) MacaronPink else TextMutedCocoa
                        )
                    }

                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "切换前后镜头",
                            tint = MacaronPink
                        )
                    }
                }
            }

            if (!hasPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFFF0F3)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "未授予相机权限",
                            color = TextDarkCocoa,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        SkeuoButton(
                            onClick = onRequestPermission,
                            backgroundColor = MacaronPink
                        ) {
                            Text("立即授权相机", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black)
                        .border(2.dp, Color(0xFFFFDDE4), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also { analysis ->
                                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                            try {
                                                val bitmap = imageProxy.toBitmap()
                                                val matrix = Matrix()
                                                var rot = imageProxy.imageInfo.rotationDegrees.toFloat()
                                                if (isFlipped180) rot += 180f
                                                matrix.postRotate(rot)
                                                val rotated = Bitmap.createBitmap(
                                                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                                                )
                                                onFrameCaptured(rotated)
                                            } catch (_: Exception) {}
                                            imageProxy.close()
                                        }
                                    }

                                val cameraSelector = CameraSelector.Builder()
                                    .requireLensFacing(lensFacing)
                                    .build()

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
                                    )
                                } catch (_: Exception) {}
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun NativeTestStreamPane(
    isConnected: Boolean,
    onSendTestPattern: (Bitmap) -> Unit
) {
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isRunning, isConnected) {
        if (isRunning && isConnected) {
            val width = 720
            val height = 1280
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = AndroidCanvas(bitmap)
            val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG)
            var frameIdx = 0

            while (isActive && isRunning) {
                canvas.drawColor(AndroidColor.parseColor("#FFF5F7"))

                paint.color = AndroidColor.parseColor("#FF8EA4")
                paint.textSize = 48f
                paint.textAlign = AndroidPaint.Align.CENTER
                canvas.drawText("🐰 MimiCast 原生推流测试", width / 2f, 220f, paint)

                paint.textSize = 34f
                paint.color = AndroidColor.parseColor("#6ED4BA")
                canvas.drawText("Frame #$frameIdx | 60FPS", width / 2f, 300f, paint)

                val bunnyY = (height * 0.5f + sin(frameIdx * 0.15f) * 60).toFloat()
                paint.textSize = 100f
                canvas.drawText("🐰 💨 📺", width / 2f, bunnyY, paint)

                onSendTestPattern(bitmap)
                frameIdx++
                delay(33) // ~30 FPS
            }
        }
    }

    SkeuoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "🐰 发送端动画推流测试",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MacaronPink,
                maxLines = 1,
                softWrap = false
            )

            Text(
                text = "向接收端大屏高频推送动态帧，实时测试网络连接与 HDMI 显示延迟。",
                fontSize = 11.sp,
                color = TextMutedCocoa,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            SkeuoButton(
                onClick = { isRunning = !isRunning },
                backgroundColor = if (isRunning) Color(0xFFFF6B6B) else MacaronMint,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRunning) "停止推流测试" else "启动动画推流测试",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
