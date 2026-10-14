package com.example.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.util.Base64
import android.util.Log
import com.example.model.CastFrame
import com.example.model.CastStatus
import com.example.model.ClientInfo
import com.example.model.IncomingConnectionRequest
import com.example.model.TelemetryData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.math.sin

class MimiStreamServer(
    private val scope: CoroutineScope
) {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private var testGeneratorJob: Job? = null

    private val _status = MutableStateFlow(CastStatus.STOPPED)
    val status: StateFlow<CastStatus> = _status.asStateFlow()

    private val _latestFrame = MutableStateFlow<CastFrame?>(null)
    val latestFrame: StateFlow<CastFrame?> = _latestFrame.asStateFlow()

    private val _telemetry = MutableStateFlow(TelemetryData())
    val telemetry: StateFlow<TelemetryData> = _telemetry.asStateFlow()

    private val _connectedClient = MutableStateFlow<ClientInfo?>(null)
    val connectedClient: StateFlow<ClientInfo?> = _connectedClient.asStateFlow()

    // Connection Approval Flow
    private val _pendingRequest = MutableStateFlow<IncomingConnectionRequest?>(null)
    val pendingRequest: StateFlow<IncomingConnectionRequest?> = _pendingRequest.asStateFlow()
    private var currentApprovalDeferred: CompletableDeferred<Boolean>? = null
    var autoApprove: Boolean = false

    // Telemetry tracking
    private var frameCountInWindow = 0
    private var bytesCountInWindow = 0L
    private var totalFrames = 0L
    private var totalBytes = 0L
    private var lastTelemetryWindowTime = System.currentTimeMillis()
    private val bitmapOptions = BitmapFactory.Options().apply {
        inPreferredConfig = Bitmap.Config.RGB_565 // Fast low-latency decoding
    }

    fun startServer(port: Int = 8080) {
        if (isRunning) return
        isRunning = true
        _status.value = CastStatus.WAITING

        scope.launch(Dispatchers.IO) {
            try {
                val socket = ServerSocket(port)
                serverSocket = socket
                Log.d("MimiStreamServer", "Server listening on port $port")

                while (isActive && isRunning && !socket.isClosed) {
                    try {
                        val clientSocket = socket.accept()
                        handleClient(clientSocket)
                    } catch (e: Exception) {
                        if (!isRunning) break
                        Log.e("MimiStreamServer", "Error accepting client", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("MimiStreamServer", "Server socket error", e)
                _status.value = CastStatus.STOPPED
            }
        }
    }

    private fun handleClient(socket: Socket) {
        scope.launch(Dispatchers.IO) {
            try {
                socket.tcpNoDelay = true
                val input = socket.getInputStream()
                val output = socket.getOutputStream()

                // Read HTTP request header
                val buffer = ByteArray(4096)
                val read = input.read(buffer)
                if (read <= 0) {
                    socket.close()
                    return@launch
                }

                val requestText = String(buffer, 0, read)
                val clientIp = socket.inetAddress.hostAddress ?: "Unknown"

                if (requestText.contains("Upgrade: websocket", ignoreCase = true)) {
                    // Handle WebSocket Upgrade for ultra-low latency frame streaming
                    performWebSocketHandshake(requestText, output)
                    handleWebSocketStream(socket, input, output, clientIp)
                } else if (requestText.startsWith("GET /")) {
                    // Serve Web Cast Page for Mobile Browser pairing
                    serveWebCastPage(output)
                    socket.close()
                } else {
                    socket.close()
                }
            } catch (e: Exception) {
                Log.e("MimiStreamServer", "Client handler error", e)
                try { socket.close() } catch (_: Exception) {}
            }
        }
    }

    private fun performWebSocketHandshake(request: String, output: OutputStream) {
        val keyLine = request.lines().firstOrNull { it.startsWith("Sec-WebSocket-Key:", ignoreCase = true) }
        val key = keyLine?.substringAfter(":")?.trim() ?: ""
        val acceptKey = Base64.encodeToString(
            MessageDigest.getInstance("SHA-1").digest((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").toByteArray()),
            Base64.NO_WRAP
        )

        val response = "HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: $acceptKey\r\n\r\n"
        output.write(response.toByteArray())
        output.flush()
    }

    private suspend fun handleWebSocketStream(socket: Socket, input: InputStream, output: OutputStream, clientIp: String) {
        var isApproved = autoApprove || clientIp == "127.0.0.1"
        var clientName = "移动设备 ($clientIp)"
        var clientPlatform = "网络同屏客户端"

        try {
            while (scope.isActive && isRunning && !socket.isClosed) {
                val b0 = input.read()
                if (b0 == -1) break
                val opcode = b0 and 0x0F

                val b1 = input.read()
                if (b1 == -1) break
                val isMasked = (b1 and 0x80) != 0
                var payloadLength = (b1 and 0x7F).toLong()

                if (payloadLength == 126L) {
                    val high = input.read()
                    val low = input.read()
                    if (high == -1 || low == -1) break
                    payloadLength = ((high shl 8) or low).toLong()
                } else if (payloadLength == 127L) {
                    var len = 0L
                    for (i in 0 until 8) {
                        val b = input.read()
                        if (b == -1) break
                        len = (len shl 8) or (b.toLong() and 0xFF)
                    }
                    payloadLength = len
                }

                val maskingKey = ByteArray(4)
                if (isMasked) {
                    readFully(input, maskingKey)
                }

                if (opcode == 0x08) {
                    // Close frame
                    break
                } else if (opcode == 0x09) {
                    // Ping -> Send Pong
                    sendWebSocketPong(output)
                    continue
                }

                if (payloadLength > 20_000_000L || payloadLength < 0) {
                    break
                }

                val payload = ByteArray(payloadLength.toInt())
                readFully(input, payload)

                if (isMasked) {
                    for (i in payload.indices) {
                        payload[i] = (payload[i].toInt() xor maskingKey[i % 4].toInt()).toByte()
                    }
                }

                if (opcode == 0x01) { // Text frame (Control / Handshake)
                    val json = String(payload, Charsets.UTF_8)
                    if (json.contains("client_hello")) {
                        val parsedName = json.substringAfter("\"name\":\"", "").substringBefore("\"")
                        val parsedPlatform = json.substringAfter("\"platform\":\"", "").substringBefore("\"")
                        if (parsedName.isNotBlank()) clientName = parsedName
                        if (parsedPlatform.isNotBlank()) clientPlatform = parsedPlatform

                        if (!isApproved) {
                            // Prompt receiver user for approval
                            val deferred = CompletableDeferred<Boolean>()
                            currentApprovalDeferred = deferred
                            _pendingRequest.value = IncomingConnectionRequest(
                                clientIp = clientIp,
                                deviceName = clientName,
                                platform = clientPlatform
                            )

                            // Wait up to 30 seconds for user approval
                            val decision = withTimeoutOrNull(30_000L) {
                                deferred.await()
                            } ?: false

                            _pendingRequest.value = null
                            currentApprovalDeferred = null

                            if (decision) {
                                isApproved = true
                                sendWebSocketText(output, """{"type":"connect_response","approved":true}""")
                                _status.value = CastStatus.STREAMING
                                _connectedClient.value = ClientInfo(
                                    ip = clientIp,
                                    name = clientName,
                                    platform = clientPlatform,
                                    connectedTime = System.currentTimeMillis()
                                )
                            } else {
                                sendWebSocketText(output, """{"type":"connect_response","approved":false,"reason":"接收端已拒绝连接请求"}""")
                                break
                            }
                        } else {
                            sendWebSocketText(output, """{"type":"connect_response","approved":true}""")
                            _status.value = CastStatus.STREAMING
                            _connectedClient.value = ClientInfo(
                                ip = clientIp,
                                name = clientName,
                                platform = clientPlatform,
                                connectedTime = System.currentTimeMillis()
                            )
                        }
                    }
                } else if (opcode == 0x02) { // Binary Frame (Video/Whiteboard frame)
                    if (isApproved) {
                        processBinaryFrame(payload)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("MimiStreamServer", "WebSocket connection ended: ${e.message}")
        } finally {
            try { socket.close() } catch (_: Exception) {}
            if (_connectedClient.value?.ip == clientIp) {
                _connectedClient.value = null
                _status.value = if (isRunning) CastStatus.WAITING else CastStatus.STOPPED
            }
        }
    }

    fun respondToConnectionRequest(approved: Boolean) {
        currentApprovalDeferred?.complete(approved)
    }

    private fun readFully(input: InputStream, buffer: ByteArray) {
        var offset = 0
        while (offset < buffer.size) {
            val count = input.read(buffer, offset, buffer.size - offset)
            if (count < 0) throw Exception("Stream closed unexpectedly")
            offset += count
        }
    }

    private fun processBinaryFrame(data: ByteArray) {
        if (data.size < 8) return

        val receiveTime = System.currentTimeMillis()
        val byteBuf = ByteBuffer.wrap(data)
        val senderTimestamp = byteBuf.long

        val imageOffset = 8
        val imageLength = data.size - 8
        if (imageLength <= 0) return

        val bitmap = BitmapFactory.decodeByteArray(data, imageOffset, imageLength, bitmapOptions) ?: return

        val latency = (receiveTime - senderTimestamp).coerceAtLeast(0).coerceAtMost(5000)

        // Update telemetry
        frameCountInWindow++
        bytesCountInWindow += data.size
        totalFrames++
        totalBytes += data.size

        val now = System.currentTimeMillis()
        if (now - lastTelemetryWindowTime >= 1000) {
            val durationSec = (now - lastTelemetryWindowTime) / 1000f
            val currentFps = (frameCountInWindow / durationSec).toInt()
            val currentBitrate = (bytesCountInWindow * 8f) / (durationSec * 1_000_000f)

            _telemetry.value = TelemetryData(
                fps = currentFps,
                latencyMs = latency,
                bitrateMbps = (Math.round(currentBitrate * 10) / 10f),
                width = bitmap.width,
                height = bitmap.height,
                totalFrames = totalFrames,
                totalBytes = totalBytes,
                lastFrameTimeMs = now
            )

            frameCountInWindow = 0
            bytesCountInWindow = 0L
            lastTelemetryWindowTime = now
        } else {
            _telemetry.value = _telemetry.value.copy(
                latencyMs = latency,
                width = bitmap.width,
                height = bitmap.height,
                totalFrames = totalFrames
            )
        }

        _latestFrame.value = CastFrame(
            bitmap = bitmap,
            senderTimestamp = senderTimestamp,
            receiveTimestamp = receiveTime,
            sizeBytes = data.size
        )
    }

    private fun sendWebSocketText(output: OutputStream, text: String) {
        try {
            val bytes = text.toByteArray(Charsets.UTF_8)
            output.write(0x81) // FIN + text
            if (bytes.size <= 125) {
                output.write(bytes.size)
            } else if (bytes.size <= 65535) {
                output.write(126)
                output.write((bytes.size shr 8) and 0xFF)
                output.write(bytes.size and 0xFF)
            } else {
                output.write(127)
                for (i in 7 downTo 0) {
                    output.write(((bytes.size.toLong() shr (i * 8)) and 0xFF).toInt())
                }
            }
            output.write(bytes)
            output.flush()
        } catch (_: Exception) {}
    }

    private fun sendWebSocketPong(output: OutputStream) {
        try {
            output.write(byteArrayOf(0x8A.toByte(), 0x00))
            output.flush()
        } catch (_: Exception) {}
    }

    private fun serveWebCastPage(output: OutputStream) {
        val html = WebCastHtmlTemplate.HTML_PAGE
        val response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: ${html.toByteArray(Charsets.UTF_8).size}\r\n" +
                "Connection: close\r\n\r\n" +
                html
        output.write(response.toByteArray(Charsets.UTF_8))
        output.flush()
    }

    fun startTestPatternGenerator() {
        if (testGeneratorJob != null) return
        _status.value = CastStatus.STREAMING
        _connectedClient.value = ClientInfo(
            ip = "127.0.0.1",
            name = "MimiCast 虚拟发生器",
            platform = "本地极速测试流",
            connectedTime = System.currentTimeMillis()
        )

        testGeneratorJob = scope.launch(Dispatchers.Default) {
            val width = 1080
            val height = 1920
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            var frameIndex = 0
            while (isActive) {
                val startTime = System.currentTimeMillis()

                canvas.drawColor(AndroidColor.parseColor("#FFF5F7"))

                for (i in 0..4) {
                    val cx = (width * (0.2f + i * 0.15f) + sin((frameIndex + i * 30) * 0.05f) * 40).toFloat()
                    val cy = (height * 0.35f + sin((frameIndex * 0.04f) + i) * 80).toFloat()
                    paint.color = AndroidColor.parseColor(
                        when (i % 4) {
                            0 -> "#FFE0E6"
                            1 -> "#E8F8F5"
                            2 -> "#FFF9E6"
                            else -> "#F3E5F5"
                        }
                    )
                    canvas.drawCircle(cx, cy, 140f, paint)
                }

                paint.color = AndroidColor.parseColor("#4A3E3D")
                paint.textSize = 64f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("🐰 糖心同屏极速测试流 🐰", width / 2f, 320f, paint)

                paint.textSize = 42f
                paint.color = AndroidColor.parseColor("#8D7F81")
                canvas.drawText("Type-C 转 HDMI & WiFi 极速接收中", width / 2f, 400f, paint)

                val bunnyY = (height * 0.55f + sin(frameIndex * 0.1f) * 60).toFloat()
                paint.textSize = 140f
                canvas.drawText("🐰 💨 📱", width / 2f, bunnyY, paint)

                paint.textSize = 52f
                paint.color = AndroidColor.parseColor("#E56A82")
                val timeStr = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
                canvas.drawText("实时基准时间: $timeStr", width / 2f, height * 0.72f, paint)

                paint.textSize = 40f
                paint.color = AndroidColor.parseColor("#6ED4BA")
                canvas.drawText("Frame #$frameIndex | 60 FPS Target", width / 2f, height * 0.78f, paint)

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)
                val jpegBytes = baos.toByteArray()

                val frameBuffer = ByteBuffer.allocate(8 + jpegBytes.size)
                frameBuffer.putLong(startTime - 12)
                frameBuffer.put(jpegBytes)

                processBinaryFrame(frameBuffer.array())

                frameIndex++
                val elapsed = System.currentTimeMillis() - startTime
                val sleepNeeded = (16 - elapsed).coerceAtLeast(1)
                delay(sleepNeeded)
            }
        }
    }

    fun stopTestPatternGenerator() {
        testGeneratorJob?.cancel()
        testGeneratorJob = null
        if (_connectedClient.value?.ip == "127.0.0.1") {
            _connectedClient.value = null
            _status.value = CastStatus.WAITING
        }
    }

    fun stopServer() {
        isRunning = false
        stopTestPatternGenerator()
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
        _status.value = CastStatus.STOPPED
        _connectedClient.value = null
        _pendingRequest.value = null
    }
}
