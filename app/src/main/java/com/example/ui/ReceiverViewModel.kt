package com.example.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hdmi.HdmiDisplayManager
import com.example.model.AspectRatioMode
import com.example.model.CastFrame
import com.example.model.CastStatus
import com.example.model.ClientInfo
import com.example.model.DeviceInfo
import com.example.model.HdmiOutputState
import com.example.model.IncomingConnectionRequest
import com.example.model.OrientationConfig
import com.example.model.RotationAngle
import com.example.model.TelemetryData
import com.example.network.MimiStreamClient
import com.example.network.MimiStreamServer
import com.example.network.NetworkUtils
import com.example.network.NsdHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppMode(val label: String) {
    RECEIVER("📺 接收端"),
    SENDER("📱 发送端")
}

class ReceiverViewModel(application: Application) : AndroidViewModel(application) {

    private val streamServer = MimiStreamServer(viewModelScope)
    val streamClient = MimiStreamClient(viewModelScope)
    private val nsdHelper = NsdHelper(application)

    private val _appMode = MutableStateFlow(AppMode.RECEIVER)
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    private val _aspectRatioMode = MutableStateFlow(AspectRatioMode.FIT)
    val aspectRatioMode: StateFlow<AspectRatioMode> = _aspectRatioMode.asStateFlow()

    private val _orientationConfig = MutableStateFlow(OrientationConfig())
    val orientationConfig: StateFlow<OrientationConfig> = _orientationConfig.asStateFlow()

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    private val _showHdmiDialog = MutableStateFlow(false)
    val showHdmiDialog: StateFlow<Boolean> = _showHdmiDialog.asStateFlow()

    private val _showPairingDialog = MutableStateFlow(false)
    val showPairingDialog: StateFlow<Boolean> = _showPairingDialog.asStateFlow()

    private val _showQrScanner = MutableStateFlow(false)
    val showQrScanner: StateFlow<Boolean> = _showQrScanner.asStateFlow()

    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo: StateFlow<DeviceInfo> = _deviceInfo.asStateFlow()

    private val hdmiManager = HdmiDisplayManager(
        context = application,
        frameFlow = streamServer.latestFrame,
        aspectRatioFlow = _aspectRatioMode
    )

    val status: StateFlow<CastStatus> = streamServer.status
    val latestFrame: StateFlow<CastFrame?> = streamServer.latestFrame
    val telemetry: StateFlow<TelemetryData> = streamServer.telemetry
    val connectedClient: StateFlow<ClientInfo?> = streamServer.connectedClient
    val hdmiState: StateFlow<HdmiOutputState> = hdmiManager.hdmiState
    val pendingConnectionRequest: StateFlow<IncomingConnectionRequest?> = streamServer.pendingRequest

    init {
        refreshNetworkInfo()
        hdmiManager.startListening()
        startReceiver()
    }

    fun setAppMode(mode: AppMode) {
        _appMode.value = mode
    }

    fun refreshNetworkInfo() {
        val app = getApplication<Application>()
        val ip = NetworkUtils.getLocalIpAddress(app)
        val ssid = NetworkUtils.getWifiSsid(app)
        val port = 8080
        val model = NetworkUtils.getDeviceModelName()

        _deviceInfo.value = DeviceInfo(
            ip = ip,
            port = port,
            ssid = ssid,
            deviceModel = model,
            webPairingUrl = "http://$ip:$port"
        )
    }

    fun startReceiver() {
        val port = _deviceInfo.value.port
        streamServer.startServer(port)
        nsdHelper.registerService(port, "MimiCast-${_deviceInfo.value.deviceModel}")
    }

    fun stopReceiver() {
        streamServer.stopServer()
        nsdHelper.unregisterService()
    }

    fun acceptConnectionRequest() {
        streamServer.respondToConnectionRequest(true)
        Toast.makeText(getApplication(), "已允许设备连接同屏", Toast.LENGTH_SHORT).show()
    }

    fun rejectConnectionRequest() {
        streamServer.respondToConnectionRequest(false)
        Toast.makeText(getApplication(), "已拒绝该设备连接", Toast.LENGTH_SHORT).show()
    }

    fun toggleAspectMode() {
        _aspectRatioMode.value = when (_aspectRatioMode.value) {
            AspectRatioMode.FIT -> AspectRatioMode.FILL
            AspectRatioMode.FILL -> AspectRatioMode.ORIGINAL_16_9
            AspectRatioMode.ORIGINAL_16_9 -> AspectRatioMode.FIT
        }
    }

    fun setAspectMode(mode: AspectRatioMode) {
        _aspectRatioMode.value = mode
    }

    fun cycleRotation() {
        _orientationConfig.value = _orientationConfig.value.toggleRotate()
    }

    fun toggleFlip180() {
        // Correct upside-down and horizontally flipped feed
        _orientationConfig.value = _orientationConfig.value.copy(
            isFlippedHorizontally = !_orientationConfig.value.isFlippedHorizontally,
            isFlippedVertically = !_orientationConfig.value.isFlippedVertically
        )
        Toast.makeText(getApplication(), "已纠正 180° 翻转画面", Toast.LENGTH_SHORT).show()
    }

    fun toggleFullscreen() {
        _isFullscreen.value = !_isFullscreen.value
    }

    fun setFullscreen(fullscreen: Boolean) {
        _isFullscreen.value = fullscreen
    }

    fun toggleHdmiPresentation() {
        hdmiManager.togglePresentation()
    }

    fun setShowHdmiDialog(show: Boolean) {
        _showHdmiDialog.value = show
    }

    fun setShowPairingDialog(show: Boolean) {
        _showPairingDialog.value = show
    }

    fun setShowQrScanner(show: Boolean) {
        _showQrScanner.value = show
    }

    fun startSelfTestStream() {
        streamServer.startTestPatternGenerator()
        Toast.makeText(getApplication(), "已启动 60FPS 极速自检同屏信号", Toast.LENGTH_SHORT).show()
    }

    fun stopSelfTestStream() {
        streamServer.stopTestPatternGenerator()
    }

    fun captureSnapshot() {
        val frame = latestFrame.value
        if (frame?.bitmap != null && !frame.bitmap.isRecycled) {
            Toast.makeText(getApplication(), "已截取当前同屏画面 (${frame.bitmap.width}x${frame.bitmap.height})", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(getApplication(), "当前无有效同屏画面", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        hdmiManager.stopListening()
        stopReceiver()
        streamClient.disconnect()
    }
}
