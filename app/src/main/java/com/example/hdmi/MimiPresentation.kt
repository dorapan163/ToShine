package com.example.hdmi

import android.app.Presentation
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Display
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioMode
import com.example.model.CastFrame
import com.example.model.OrientationConfig
import kotlinx.coroutines.flow.StateFlow

class MimiPresentation(
    outerContext: Context,
    display: Display,
    private val frameFlow: StateFlow<CastFrame?>,
    private val aspectRatioFlow: StateFlow<AspectRatioMode>,
    private val orientationFlow: StateFlow<OrientationConfig>? = null
) : Presentation(outerContext, display) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        val composeView = ComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setContent {
                val currentFrame by frameFlow.collectAsState()
                val aspectMode by aspectRatioFlow.collectAsState()
                val orientation by (orientationFlow?.collectAsState() ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(OrientationConfig()) })

                HdmiPresentationContent(
                    frame = currentFrame,
                    aspectRatioMode = aspectMode,
                    orientation = orientation,
                    displayName = display.name
                )
            }
        }

        setContentView(composeView)
    }
}

@Composable
fun HdmiPresentationContent(
    frame: CastFrame?,
    aspectRatioMode: AspectRatioMode,
    orientation: OrientationConfig,
    displayName: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E212D)),
        contentAlignment = Alignment.Center
    ) {
        if (frame?.bitmap != null && !frame.bitmap.isRecycled) {
            val contentScale = when (aspectRatioMode) {
                AspectRatioMode.FIT -> ContentScale.Fit
                AspectRatioMode.FILL -> ContentScale.FillBounds
                AspectRatioMode.ORIGINAL_16_9 -> ContentScale.Inside
            }

            Image(
                bitmap = frame.bitmap.asImageBitmap(),
                contentDescription = "HDMI 投屏镜像画面",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = orientation.rotation.degrees.toFloat()
                        scaleX = if (orientation.isFlippedHorizontally) -1f else 1f
                        scaleY = if (orientation.isFlippedVertically) -1f else 1f
                    },
                contentScale = contentScale
            )

            // Subtle top-right HDMI status badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0x99000000)
            ) {
                Text(
                    text = "📺 Type-C HDMI | ${frame.bitmap.width}x${frame.bitmap.height}",
                    color = Color(0xFF6ED4BA),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        } else {
            // Standby screen on HDMI monitor
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFF0F3)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🐰 MimiCast 糖心大屏已连接 ($displayName)\n请在手机发送端开启同屏",
                            color = Color(0xFFFF8EA4),
                            fontSize = 20.sp,
                            lineHeight = 28.sp
                        )
                    }
                }
            }
        }
    }
}
