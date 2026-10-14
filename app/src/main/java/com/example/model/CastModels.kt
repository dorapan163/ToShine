package com.example.model

import android.graphics.Bitmap

enum class CastStatus(val label: String) {
    STOPPED("服务已停止"),
    WAITING("等待设备连接中"),
    CONNECTING("正在握手连接"),
    STREAMING("正在同步画面"),
    PAUSED("同步已暂停")
}

data class TelemetryData(
    val fps: Int = 0,
    val latencyMs: Long = 0,
    val bitrateMbps: Float = 0f,
    val width: Int = 0,
    val height: Int = 0,
    val totalFrames: Long = 0,
    val totalBytes: Long = 0,
    val lastFrameTimeMs: Long = 0
)

data class DeviceInfo(
    val ip: String = "127.0.0.1",
    val port: Int = 8080,
    val ssid: String = "未知 Wi-Fi",
    val deviceModel: String = "Android Receiver",
    val webPairingUrl: String = "http://127.0.0.1:8080"
)

data class ClientInfo(
    val ip: String = "",
    val name: String = "",
    val platform: String = "Unknown",
    val connectedTime: Long = System.currentTimeMillis()
)

data class IncomingConnectionRequest(
    val id: String = java.util.UUID.randomUUID().toString(),
    val clientIp: String,
    val deviceName: String,
    val platform: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ClientApprovalState(val label: String) {
    IDLE("未连接"),
    CONNECTING("正在连接…"),
    WAITING_APPROVAL("等待接收端同意…"),
    APPROVED("已获批准，推流中"),
    REJECTED("接收端拒绝了连接请求")
}

data class HdmiOutputState(
    val isConnected: Boolean = false,
    val displayId: Int = -1,
    val displayName: String = "",
    val resolution: String = "",
    val refreshRate: Float = 60f,
    val isPresenting: Boolean = false
)

enum class AspectRatioMode(val label: String) {
    FIT("等比适应"),
    FILL("拉伸铺满"),
    ORIGINAL_16_9("16:9 标准")
}

enum class RotationAngle(val degrees: Int) {
    ROT_0(0),
    ROT_90(90),
    ROT_180(180),
    ROT_270(270);

    fun next(): RotationAngle = when (this) {
        ROT_0 -> ROT_90
        ROT_90 -> ROT_180
        ROT_180 -> ROT_270
        ROT_270 -> ROT_0
    }
}

data class OrientationConfig(
    val rotation: RotationAngle = RotationAngle.ROT_0,
    val isFlippedHorizontally: Boolean = false,
    val isFlippedVertically: Boolean = false
) {
    fun toggleRotate(): OrientationConfig = copy(rotation = rotation.next())
    fun toggleFlipHorizontal(): OrientationConfig = copy(isFlippedHorizontally = !isFlippedHorizontally)
    fun toggleFlipVertical(): OrientationConfig = copy(isFlippedVertically = !isFlippedVertically)
    fun reset(): OrientationConfig = OrientationConfig()
}

data class CastFrame(
    val bitmap: Bitmap,
    val senderTimestamp: Long,
    val receiveTimestamp: Long = System.currentTimeMillis(),
    val sizeBytes: Int
)
