package com.example.network

object WebCastHtmlTemplate {
    val HTML_PAGE = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>MimiCast 糖心同屏发送端</title>
  <style>
    * { box-sizing: border-box; -webkit-tap-highlight-color: transparent; }
    body {
      margin: 0; padding: 14px;
      background: #FFF0F3;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "PingFang SC", sans-serif;
      color: #4A3E3D;
      display: flex; flex-direction: column; align-items: center;
      min-height: 100vh;
    }
    .container {
      width: 100%; max-width: 480px;
      display: flex; flex-direction: column; gap: 14px;
    }
    .header-card {
      background: #FFFFFF;
      border-radius: 26px;
      padding: 18px;
      text-align: center;
      box-shadow: 0 8px 24px rgba(229, 106, 130, 0.15), inset 0 2px 4px rgba(255,255,255,0.8);
      border: 3px solid #FFDDE4;
    }
    .badge {
      display: inline-block;
      background: #6ED4BA;
      color: #FFFFFF;
      font-size: 11px; font-weight: bold;
      padding: 4px 12px;
      border-radius: 20px;
      margin-bottom: 6px;
      letter-spacing: 0.5px;
    }
    h1 {
      margin: 2px 0 6px 0;
      font-size: 20px;
      color: #FF8EA4;
    }
    .subtitle {
      font-size: 12px;
      color: #8D7F81;
      margin: 0;
    }
    .status-pill {
      margin-top: 10px;
      display: inline-flex; align-items: center; gap: 8px;
      background: #FFF5F7;
      border: 2px solid #FFDDE4;
      padding: 5px 12px;
      border-radius: 16px;
      font-size: 12px; font-weight: 600;
    }
    .dot {
      width: 10px; height: 10px;
      border-radius: 50%;
      background: #6ED4BA;
      box-shadow: 0 0 8px #6ED4BA;
      animation: pulse 1.5s infinite;
    }
    @keyframes pulse {
      0% { opacity: 0.6; transform: scale(0.9); }
      50% { opacity: 1; transform: scale(1.15); }
      100% { opacity: 0.6; transform: scale(0.9); }
    }
    .tabs {
      display: flex; gap: 6px;
      background: #FFFFFF;
      padding: 5px;
      border-radius: 20px;
      border: 2px solid #FFDDE4;
    }
    .tab-btn {
      flex: 1;
      padding: 9px 2px;
      border: none;
      background: transparent;
      border-radius: 15px;
      font-size: 12px; font-weight: bold;
      color: #8D7F81;
      cursor: pointer;
      transition: all 0.2s;
    }
    .tab-btn.active {
      background: #FF8EA4;
      color: #FFFFFF;
      box-shadow: 0 4px 10px rgba(255, 142, 164, 0.4);
    }
    .card {
      background: #FFFFFF;
      border-radius: 24px;
      padding: 16px;
      box-shadow: 0 6px 18px rgba(229, 106, 130, 0.1);
      border: 2px solid #FFDDE4;
      display: flex; flex-direction: column; align-items: center;
    }
    .action-btn {
      width: 100%;
      padding: 15px;
      background: linear-gradient(135deg, #FF8EA4, #FF708F);
      color: white;
      border: none;
      border-radius: 20px;
      font-size: 15px; font-weight: bold;
      cursor: pointer;
      display: flex; align-items: center; justify-content: center; gap: 8px;
      box-shadow: 0 6px 16px rgba(255, 112, 143, 0.35);
      transition: transform 0.1s, box-shadow 0.1s;
    }
    .action-btn:active {
      transform: translateY(2px);
      box-shadow: 0 2px 8px rgba(255, 112, 143, 0.25);
    }
    .action-btn.stop {
      background: linear-gradient(135deg, #FF6B6B, #E05252);
      box-shadow: 0 6px 16px rgba(255, 107, 107, 0.35);
    }
    .action-btn.mint {
      background: linear-gradient(135deg, #6ED4BA, #45B395);
      box-shadow: 0 6px 16px rgba(110, 212, 186, 0.35);
    }
    .action-btn.butter {
      background: linear-gradient(135deg, #FFD166, #E0A720);
      color: #4A3E3D;
      box-shadow: 0 6px 16px rgba(255, 209, 102, 0.35);
    }
    .notice-box {
      width: 100%;
      background: #FFF9E6;
      border: 1.5px solid #FFE082;
      border-radius: 16px;
      padding: 12px;
      margin-bottom: 12px;
      font-size: 12px;
      color: #795548;
      line-height: 1.5;
    }
    .notice-box b {
      color: #E65100;
    }
    .preview-box {
      width: 100%; height: 260px;
      background: #2B2D42;
      border-radius: 18px;
      margin-top: 12px;
      overflow: hidden;
      display: flex; align-items: center; justify-content: center;
      position: relative;
      border: 3px solid #FFE0E6;
    }
    video, canvas, img {
      max-width: 100%; max-height: 100%;
      object-fit: contain;
    }
    .draw-canvas {
      background: #FFFFFF;
      border-radius: 16px;
      touch-action: none;
      cursor: crosshair;
    }
    .palette {
      display: flex; gap: 10px; margin-top: 10px;
    }
    .color-dot {
      width: 30px; height: 30px;
      border-radius: 50%;
      border: 3px solid #FFF;
      box-shadow: 0 2px 6px rgba(0,0,0,0.15);
      cursor: pointer;
    }
    .stats-bar {
      width: 100%;
      display: flex; justify-content: space-around;
      margin-top: 12px;
      font-size: 11px; color: #8D7F81;
      background: #FFF5F7;
      padding: 8px; border-radius: 12px;
    }
    .stamp-bar {
      display: flex; gap: 6px; margin-top: 10px; flex-wrap: wrap; justify-content: center;
    }
    .stamp-btn {
      font-size: 20px; background: #FFF5F7; border: 1px solid #FFDDE4;
      border-radius: 12px; padding: 4px 8px; cursor: pointer;
    }
    .toggle-row {
      display: flex; align-items: center; gap: 8px;
      margin-top: 10px; font-size: 12px; color: #6D5E60;
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="header-card">
      <div class="badge">WIFI 局域网同屏发送端</div>
      <h1>🐰 糖心同屏 MimiCast</h1>
      <p class="subtitle">与接收端连接同一 WiFi，选择任意方式开始同屏</p>
      <div class="status-pill">
        <span class="dot" id="wsDot"></span>
        <span id="wsStatus">正在连接接收端...</span>
      </div>
    </div>

    <div class="tabs">
      <button class="tab-btn active" onclick="switchTab('camera')">📷 相机同屏</button>
      <button class="tab-btn" onclick="switchTab('screen')">📱 屏幕同屏</button>
      <button class="tab-btn" onclick="switchTab('draw')">🎨 互动画板</button>
      <button class="tab-btn" onclick="switchTab('photo')">🖼️ 照片直投</button>
    </div>

    <!-- Camera Cast Pane -->
    <div class="card" id="pane-camera">
      <!-- Insecure Context Friendly Banner -->
      <div class="notice-box" id="cameraNotice">
        💡 <b>HTTP 极速直连模式已启用</b>：移动端浏览器在非 HTTPS 局域网下默认受限，本应用已自动适配<b>免证书相机直拍与连拍通道</b>，点击下方按钮即可实时同屏！
      </div>

      <!-- HTTP Direct Camera Trigger (Zero permission / Insecure Context Safe) -->
      <input type="file" id="httpCameraInput" accept="image/*" capture="environment" style="display:none;" onchange="handleLiveCameraShot(this)">

      <button class="action-btn mint" id="btnHttpCamera" onclick="triggerHttpCamera()">
        <span>📷 点击拍照实时同屏 (免证书)</span>
      </button>

      <!-- WebRTC Continuous Camera (if supported by environment) -->
      <button class="action-btn butter" id="btnWebRtcCamera" style="margin-top:8px; display:none;" onclick="toggleWebRtcCamera()">
        <span>📹 尝试启动连续视频流同屏</span>
      </button>

      <div class="toggle-row">
        <label style="display:flex; align-items:center; gap:6px; cursor:pointer;">
          <input type="checkbox" id="chkContinuousShot" checked>
          <span>📸 开启极速连拍模式（拍完自动就绪下一张）</span>
        </label>
      </div>

      <div class="preview-box">
        <video id="cameraVideo" autoplay playsinline muted style="display:none;"></video>
        <img id="cameraImgPreview" style="display:none;">
        <span id="cameraPlaceholder" style="color:#FFF; font-size:13px; text-align:center; padding:12px;">
          点击上方按钮拍照<br>画面将毫秒级同步传输到大屏
        </span>
      </div>

      <div class="stats-bar">
        <span>已发送帧: <b id="statCamFrames">0</b></span>
        <span>传输耗时: <b id="statCamLatency">~12</b> ms</span>
        <span>连接: <b>高速 WebSocket</b></span>
      </div>
    </div>

    <!-- Screen Cast Pane -->
    <div class="card" id="pane-screen" style="display:none;">
      <div class="notice-box" id="screenNotice">
        💡 提示：手机端操作系统通常限制普通移动浏览器直接捕获桌面屏幕。如需流畅同屏手机桌面/游戏，推荐在发送端手机同样安装 <b>MimiCast App</b> 开启原生 60FPS 同屏；或使用上方的【📷 相机同屏】与【🎨 互动画板】。
      </div>

      <button class="action-btn" id="btnScreen" onclick="toggleScreenShare()">
        <span>🚀 尝试启动屏幕同屏 (支持电脑/平板)</span>
      </button>
      <div class="preview-box">
        <video id="screenVideo" autoplay playsinline muted></video>
        <span id="screenPlaceholder" style="color:#FFF; font-size:13px;">点击上方按钮发起同屏</span>
      </div>
      <div class="stats-bar">
        <span>帧率: <b id="statFps">0</b> FPS</span>
        <span>延迟: <b id="statPing">~12</b> ms</span>
        <span>编码: <b>Turbo JPEG</b></span>
      </div>
    </div>

    <!-- Draw Pane -->
    <div class="card" id="pane-draw" style="display:none;">
      <div style="font-size:13px; color:#8D7F81; margin-bottom:8px;">在下方白板随意涂鸦，大屏实时同步呈现</div>
      <canvas id="drawCanvas" class="draw-canvas" width="340" height="260"></canvas>
      <div class="palette">
        <div class="color-dot" style="background:#FF8EA4;" onclick="setColor('#FF8EA4')"></div>
        <div class="color-dot" style="background:#6ED4BA;" onclick="setColor('#6ED4BA')"></div>
        <div class="color-dot" style="background:#FFD166;" onclick="setColor('#FFD166')"></div>
        <div class="color-dot" style="background:#81D4FA;" onclick="setColor('#81D4FA')"></div>
        <div class="color-dot" style="background:#B39DDB;" onclick="setColor('#B39DDB')"></div>
        <div class="color-dot" style="background:#4A3E3D;" onclick="setColor('#4A3E3D')"></div>
      </div>
      <div class="stamp-bar">
        <button class="stamp-btn" onclick="addStamp('🐰')">🐰</button>
        <button class="stamp-btn" onclick="addStamp('⭐')">⭐</button>
        <button class="stamp-btn" onclick="addStamp('💖')">💖</button>
        <button class="stamp-btn" onclick="addStamp('🌸')">🌸</button>
        <button class="stamp-btn" onclick="addStamp('🍓')">🍓</button>
        <button class="stamp-btn" id="btnCanvasFlip" onclick="toggleCanvasFlip()" style="font-size:12px; font-weight:bold; color:#FF8EA4;">🔄 翻转纠正</button>
        <button class="stamp-btn" onclick="clearCanvas()">🧹 清屏</button>
      </div>
    </div>

    <!-- Photo Pane -->
    <div class="card" id="pane-photo" style="display:none;">
      <input type="file" id="photoInput" accept="image/*" style="display:none;" onchange="handlePhotoUpload(this)">
      <button class="action-btn" onclick="document.getElementById('photoInput').click()">
        <span>📂 选择相册照片同屏</span>
      </button>
      <div class="preview-box">
        <img id="photoPreview" style="max-width:100%; max-height:100%; object-fit:contain; display:none;">
        <span id="photoPlaceholder" style="color:#FFF; font-size:13px;">选择照片后立即输出至大屏</span>
      </div>
    </div>
  </div>

  <canvas id="captureCanvas" style="display:none;"></canvas>

  <script>
    let ws = null;
    let isSharing = false;
    let stream = null;
    let frameCount = 0;
    let lastFpsUpdate = Date.now();
    let currentColor = '#FF8EA4';
    let camFramesSent = 0;

    // Detect Secure Context & MediaDevices
    const isSecure = window.isSecureContext || location.hostname === 'localhost' || location.hostname === '127.0.0.1';
    const hasMediaDevices = !!(navigator && navigator.mediaDevices && navigator.mediaDevices.getUserMedia);

    window.addEventListener('DOMContentLoaded', () => {
      if (hasMediaDevices) {
        document.getElementById('btnWebRtcCamera').style.display = 'flex';
      }
    });

    function initWebSocket() {
      const loc = window.location;
      const wsUri = (loc.protocol === 'https:' ? 'wss:' : 'ws:') + '//' + loc.host + '/ws';
      ws = new WebSocket(wsUri);
      ws.binaryType = 'arraybuffer';

      ws.onopen = () => {
        document.getElementById('wsDot').style.background = '#FFD166';
        document.getElementById('wsDot').style.boxShadow = '0 0 8px #FFD166';
        document.getElementById('wsStatus').innerText = '已发送请求，等待接收端确认…';
        ws.send(JSON.stringify({
          type: 'client_hello',
          name: navigator.userAgent.includes('iPhone') ? 'iPhone 移动设备' : 'Android 移动设备',
          platform: 'WebCast 网页同屏'
        }));
      };

      ws.onmessage = (event) => {
        try {
          if (typeof event.data === 'string') {
            const data = JSON.parse(event.data);
            if (data.type === 'connect_response') {
              if (data.approved) {
                document.getElementById('wsDot').style.background = '#6ED4BA';
                document.getElementById('wsDot').style.boxShadow = '0 0 8px #6ED4BA';
                document.getElementById('wsStatus').innerText = '接收端已同意！同屏推流就绪';
              } else {
                document.getElementById('wsDot').style.background = '#FF6B6B';
                document.getElementById('wsDot').style.boxShadow = '0 0 8px #FF6B6B';
                document.getElementById('wsStatus').innerText = data.reason || '接收端拒绝了连接请求';
                alert(data.reason || '接收端拒绝了连接请求');
              }
            }
          }
        } catch (_) {}
      };

      ws.onclose = () => {
        document.getElementById('wsDot').style.background = '#FF6B6B';
        document.getElementById('wsDot').style.boxShadow = '0 0 8px #FF6B6B';
        document.getElementById('wsStatus').innerText = '已断开，正在重连...';
        setTimeout(initWebSocket, 2000);
      };

      ws.onerror = () => {
        document.getElementById('wsStatus').innerText = '连接出错';
      };
    }

    initWebSocket();

    function switchTab(tab) {
      ['camera', 'screen', 'draw', 'photo'].forEach(t => {
        document.getElementById('pane-' + t).style.display = (t === tab ? 'flex' : 'none');
      });
      document.querySelectorAll('.tab-btn').forEach((btn, idx) => {
        btn.classList.toggle('active', ['camera', 'screen', 'draw', 'photo'][idx] === tab);
      });
      if (tab === 'draw') setupDrawingCanvas();
    }

    // --- HTTP ZERO-PERMISSION CAMERA CHANNEL (100% Compatible) ---
    function triggerHttpCamera() {
      const input = document.getElementById('httpCameraInput');
      input.value = ''; // Reset input to re-trigger even on same shot
      input.click();
    }

    function handleLiveCameraShot(input) {
      if (!input.files || !input.files[0]) return;
      const file = input.files[0];
      const sendStartTime = Date.now();

      const reader = new FileReader();
      reader.onload = (e) => {
        const img = document.getElementById('cameraImgPreview');
        img.src = e.target.result;
        img.style.display = 'block';
        document.getElementById('cameraPlaceholder').style.display = 'none';
        document.getElementById('cameraVideo').style.display = 'none';

        // Downscale to 1080p for ultra-fast network push
        const offCanvas = document.createElement('canvas');
        const offCtx = offCanvas.getContext('2d');
        const image = new Image();
        image.onload = () => {
          const maxDim = 1280;
          let w = image.width, h = image.height;
          if (w > maxDim || h > maxDim) {
            if (w > h) { h = Math.round(h * maxDim / w); w = maxDim; }
            else { w = Math.round(w * maxDim / h); h = maxDim; }
          }
          offCanvas.width = w;
          offCanvas.height = h;
          offCtx.drawImage(image, 0, 0, w, h);
          offCanvas.toBlob(blob => {
            if (!blob) return;
            blob.arrayBuffer().then(buf => {
              sendBinaryFrameWithTimestamp(buf);
              camFramesSent++;
              document.getElementById('statCamFrames').innerText = camFramesSent;
              document.getElementById('statCamLatency').innerText = (Date.now() - sendStartTime) + ' ms';

              // If continuous shot enabled, prepare next
              if (document.getElementById('chkContinuousShot').checked) {
                setTimeout(() => {
                  triggerHttpCamera();
                }, 300);
              }
            });
          }, 'image/jpeg', 0.82);
        };
        image.src = e.target.result;
      };
      reader.readAsDataURL(file);
    }

    // --- WEBRTC CAMERA (When supported by secure context) ---
    async function toggleWebRtcCamera() {
      const btn = document.getElementById('btnWebRtcCamera');
      const video = document.getElementById('cameraVideo');
      const placeholder = document.getElementById('cameraPlaceholder');
      const img = document.getElementById('cameraImgPreview');

      if (isSharing) {
        stopStreaming();
        btn.innerHTML = '<span>📹 尝试启动连续视频流同屏</span>';
        btn.classList.remove('stop');
        placeholder.style.display = 'block';
        video.srcObject = null;
        video.style.display = 'none';
        isSharing = false;
        return;
      }

      if (!hasMediaDevices) {
        alert('您的移动浏览器在当前 HTTP 环境下限制直接抓取视频流，请使用上方绿色的【点击拍照实时同屏】功能！');
        return;
      }

      try {
        stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: { ideal: 'environment' }, width: { ideal: 1280 } }
        });
        video.srcObject = stream;
        video.style.display = 'block';
        img.style.display = 'none';
        placeholder.style.display = 'none';
        btn.innerHTML = '<span>⏹️ 关闭连续视频流</span>';
        btn.classList.add('stop');
        isSharing = true;

        startFrameCapture(video);
      } catch (err) {
        alert('无法启动连续相机流: ' + err.message + '。建议使用上方的【拍照实时同屏】！');
      }
    }

    // --- SCREEN CAST ---
    async function toggleScreenShare() {
      const btn = document.getElementById('btnScreen');
      const video = document.getElementById('screenVideo');
      const placeholder = document.getElementById('screenPlaceholder');

      if (isSharing) {
        stopStreaming();
        btn.innerHTML = '<span>🚀 尝试启动屏幕同屏 (支持电脑/平板)</span>';
        btn.classList.remove('stop');
        placeholder.style.display = 'block';
        video.srcObject = null;
        isSharing = false;
        return;
      }

      if (!navigator.mediaDevices || !navigator.mediaDevices.getDisplayMedia) {
        alert('手机端浏览器受操作系统安全机制限制不支持直接抓屏。\n\n推荐：\n1. 在发送端手机同样安装 MimiCast App 开启 60FPS 原生系统同屏\n2. 使用本页的【相机同屏】或【互动画板】！');
        return;
      }

      try {
        stream = await navigator.mediaDevices.getDisplayMedia({
          video: { frameRate: { ideal: 30, max: 60 } }
        });
        video.srcObject = stream;
        placeholder.style.display = 'none';
        btn.innerHTML = '<span>⏹️ 停止屏幕同屏</span>';
        btn.classList.add('stop');
        isSharing = true;

        stream.getVideoTracks()[0].onended = () => {
          if (isSharing) toggleScreenShare();
        };

        startFrameCapture(video);
      } catch (err) {
        console.error('Screen share error', err);
      }
    }

    function startFrameCapture(videoElement) {
      const captureCanvas = document.getElementById('captureCanvas');
      const ctx = captureCanvas.getContext('2d');

      const sendFrame = () => {
        if (!isSharing || !ws || ws.readyState !== WebSocket.OPEN) return;
        if (videoElement.videoWidth === 0 || videoElement.videoHeight === 0) {
          requestAnimationFrame(sendFrame);
          return;
        }

        const scale = Math.min(1, 960 / Math.max(videoElement.videoWidth, videoElement.videoHeight));
        captureCanvas.width = videoElement.videoWidth * scale;
        captureCanvas.height = videoElement.videoHeight * scale;
        ctx.drawImage(videoElement, 0, 0, captureCanvas.width, captureCanvas.height);

        captureCanvas.toBlob((blob) => {
          if (!blob) return;
          blob.arrayBuffer().then(buf => {
            sendBinaryFrameWithTimestamp(buf);
            frameCount++;
            const now = Date.now();
            if (now - lastFpsUpdate >= 1000) {
              const fps = Math.round(frameCount * 1000 / (now - lastFpsUpdate));
              const statFps = document.getElementById('statFps');
              if (statFps) statFps.innerText = fps;
              frameCount = 0;
              lastFpsUpdate = now;
            }
          });
        }, 'image/jpeg', 0.72);

        setTimeout(sendFrame, 33);
      };

      sendFrame();
    }

    function sendBinaryFrameWithTimestamp(arrayBuffer) {
      if (!ws || ws.readyState !== WebSocket.OPEN) return;
      const now = Date.now();
      const packet = new Uint8Array(8 + arrayBuffer.byteLength);
      const view = new DataView(packet.buffer);
      view.setBigInt64(0, BigInt(now));
      packet.set(new Uint8Array(arrayBuffer), 8);
      ws.send(packet.buffer);
    }

    function stopStreaming() {
      if (stream) {
        stream.getTracks().forEach(t => t.stop());
        stream = null;
      }
    }

    // --- WHITEBOARD ---
    let isDrawing = false;
    let dCanvas, dCtx;
    function setupDrawingCanvas() {
      dCanvas = document.getElementById('drawCanvas');
      dCtx = dCanvas.getContext('2d');
      dCtx.lineCap = 'round';
      dCtx.lineJoin = 'round';
      dCtx.lineWidth = 6;

      const getPos = (e) => {
        const rect = dCanvas.getBoundingClientRect();
        const clientX = e.touches ? e.touches[0].clientX : e.clientX;
        const clientY = e.touches ? e.touches[0].clientY : e.clientY;
        const scaleX = dCanvas.width / (rect.width || 1);
        const scaleY = dCanvas.height / (rect.height || 1);
        return {
          x: (clientX - rect.left) * scaleX,
          y: (clientY - rect.top) * scaleY
        };
      };

      const startDraw = (e) => {
        isDrawing = true;
        const pos = getPos(e);
        dCtx.beginPath();
        dCtx.strokeStyle = currentColor;
        dCtx.moveTo(pos.x, pos.y);
        e.preventDefault();
      };

      const moveDraw = (e) => {
        if (!isDrawing) return;
        const pos = getPos(e);
        dCtx.lineTo(pos.x, pos.y);
        dCtx.stroke();
        sendCanvasFrame();
        e.preventDefault();
      };

      const endDraw = () => {
        if (isDrawing) {
          isDrawing = false;
          sendCanvasFrame();
        }
      };

      dCanvas.onmousedown = startDraw;
      dCanvas.onmousemove = moveDraw;
      window.onmouseup = endDraw;

      dCanvas.ontouchstart = startDraw;
      dCanvas.ontouchmove = moveDraw;
      dCanvas.ontouchend = endDraw;
    }

    function setColor(c) {
      currentColor = c;
    }

    let isCanvasFlipped180 = false;
    function toggleCanvasFlip() {
      isCanvasFlipped180 = !isCanvasFlipped180;
      const btn = document.getElementById('btnCanvasFlip');
      if (btn) btn.innerText = isCanvasFlipped180 ? '🔄 已翻转180°' : '🔄 翻转纠正';
      sendCanvasFrame();
    }

    function addStamp(emoji) {
      if (!dCtx) setupDrawingCanvas();
      dCtx.font = '36px sans-serif';
      dCtx.fillText(emoji, 30 + Math.random() * 240, 30 + Math.random() * 180);
      sendCanvasFrame();
    }

    function clearCanvas() {
      if (dCtx) {
        dCtx.clearRect(0, 0, dCanvas.width, dCanvas.height);
        sendCanvasFrame();
      }
    }

    function sendCanvasFrame() {
      if (!dCanvas) return;
      const offCanvas = document.createElement('canvas');
      offCanvas.width = dCanvas.width;
      offCanvas.height = dCanvas.height;
      const offCtx = offCanvas.getContext('2d');
      if (isCanvasFlipped180) {
        offCtx.translate(dCanvas.width, dCanvas.height);
        offCtx.rotate(Math.PI);
      }
      offCtx.drawImage(dCanvas, 0, 0);
      offCanvas.toBlob(blob => {
        if (!blob) return;
        blob.arrayBuffer().then(buf => sendBinaryFrameWithTimestamp(buf));
      }, 'image/jpeg', 0.8);
    }

    // --- PHOTO BEAM ---
    function handlePhotoUpload(input) {
      if (!input.files || !input.files[0]) return;
      const file = input.files[0];
      const reader = new FileReader();
      reader.onload = (e) => {
        const img = document.getElementById('photoPreview');
        img.src = e.target.result;
        img.style.display = 'block';
        document.getElementById('photoPlaceholder').style.display = 'none';

        const offCanvas = document.createElement('canvas');
        const offCtx = offCanvas.getContext('2d');
        const image = new Image();
        image.onload = () => {
          const maxDim = 1280;
          let w = image.width, h = image.height;
          if (w > maxDim || h > maxDim) {
            if (w > h) { h = Math.round(h * maxDim / w); w = maxDim; }
            else { w = Math.round(w * maxDim / h); h = maxDim; }
          }
          offCanvas.width = w;
          offCanvas.height = h;
          offCtx.drawImage(image, 0, 0, w, h);
          offCanvas.toBlob(blob => {
            if (blob) {
              blob.arrayBuffer().then(buf => sendBinaryFrameWithTimestamp(buf));
            }
          }, 'image/jpeg', 0.85);
        };
        image.src = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  </script>
</body>
</html>
    """.trimIndent()
}
