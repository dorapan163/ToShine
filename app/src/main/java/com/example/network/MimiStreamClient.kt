package com.example.network

import android.graphics.Bitmap
import android.util.Log
import com.example.model.ClientApprovalState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

class MimiStreamClient(private val scope: CoroutineScope) {

    private val client = OkHttpClient.Builder()
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .pingInterval(5, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _approvalState = MutableStateFlow(ClientApprovalState.IDLE)
    val approvalState: StateFlow<ClientApprovalState> = _approvalState.asStateFlow()

    private val _framesSent = MutableStateFlow(0L)
    val framesSent: StateFlow<Long> = _framesSent.asStateFlow()

    private val _latencyMs = MutableStateFlow(12L)
    val latencyMs: StateFlow<Long> = _latencyMs.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    companion object {
        fun sanitizeHostAndPort(input: String, defaultPort: Int = 8080): Pair<String, Int> {
            var s = input.trim()
            if (s.startsWith("http://", ignoreCase = true)) s = s.substring(7)
            if (s.startsWith("https://", ignoreCase = true)) s = s.substring(8)
            if (s.startsWith("ws://", ignoreCase = true)) s = s.substring(5)
            if (s.startsWith("wss://", ignoreCase = true)) s = s.substring(6)
            s = s.substringBefore("/").trim()

            return if (s.contains(":")) {
                val host = s.substringBefore(":")
                val port = s.substringAfter(":").toIntOrNull() ?: defaultPort
                Pair(host, port)
            } else {
                Pair(s, defaultPort)
            }
        }
    }

    fun connect(targetInput: String, defaultPort: Int = 8080, deviceName: String = "Android 发送端") {
        disconnect()

        val (host, port) = sanitizeHostAndPort(targetInput, defaultPort)
        if (host.isBlank()) {
            _statusMessage.value = "请输入有效的接收端 IP 地址"
            _approvalState.value = ClientApprovalState.IDLE
            return
        }

        _approvalState.value = ClientApprovalState.CONNECTING
        _statusMessage.value = "正在连接接收端 $host:$port …"

        val url = "ws://$host:$port/ws"
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("MimiStreamClient", "WebSocket channel opened at $url")
                _approvalState.value = ClientApprovalState.WAITING_APPROVAL
                _statusMessage.value = "已发出连接请求，等待接收端同意…"

                // Send handshake and wait for approval
                webSocket.send(
                    """{"type":"client_hello","name":"$deviceName","platform":"Android 原生同屏"}"""
                )
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("MimiStreamClient", "Received text: $text")
                if (text.contains("\"approved\":true")) {
                    _isConnected.value = true
                    _approvalState.value = ClientApprovalState.APPROVED
                    _statusMessage.value = "接收端已同意！同屏推流就绪"
                } else if (text.contains("\"approved\":false")) {
                    _isConnected.value = false
                    _approvalState.value = ClientApprovalState.REJECTED
                    val reason = text.substringAfter("\"reason\":\"", "").substringBefore("\"")
                    _statusMessage.value = if (reason.isNotBlank()) reason else "接收端拒绝了本次连接请求"
                    disconnect()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
                _approvalState.value = ClientApprovalState.IDLE
                _statusMessage.value = "连接已关闭: $reason"
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
                _approvalState.value = ClientApprovalState.IDLE
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("MimiStreamClient", "Connection failed", t)
                _isConnected.value = false
                _approvalState.value = ClientApprovalState.IDLE
                _statusMessage.value = "连接失败，请检查 IP 是否正确且处于同一 Wi-Fi"
            }
        })
    }

    fun sendBitmapFrame(bitmap: Bitmap, quality: Int = 75) {
        val ws = webSocket ?: return
        if (!_isConnected.value || _approvalState.value != ClientApprovalState.APPROVED) return

        scope.launch(Dispatchers.Default) {
            try {
                val sendStartTime = System.currentTimeMillis()
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                val jpegBytes = baos.toByteArray()

                val buffer = ByteBuffer.allocate(8 + jpegBytes.size)
                buffer.putLong(sendStartTime)
                buffer.put(jpegBytes)

                val byteString = buffer.array().toByteString()
                val sent = ws.send(byteString)
                if (sent) {
                    _framesSent.value = _framesSent.value + 1
                    val elapsed = System.currentTimeMillis() - sendStartTime
                    _latencyMs.value = elapsed.coerceAtLeast(4)
                }
            } catch (e: Exception) {
                Log.e("MimiStreamClient", "Error sending frame", e)
            }
        }
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "User disconnected")
        } catch (_: Exception) {}
        webSocket = null
        _isConnected.value = false
        _approvalState.value = ClientApprovalState.IDLE
        _framesSent.value = 0
    }
}
