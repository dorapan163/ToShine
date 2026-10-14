package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AspectRatioMode
import com.example.model.CastFrame
import com.example.model.CastStatus
import com.example.model.OrientationConfig
import com.example.ui.theme.MacaronButter
import com.example.ui.theme.MacaronMint
import com.example.ui.theme.MacaronPink
import com.example.ui.theme.MacaronSky
import com.example.ui.theme.SkeuoScreenBg
import com.example.ui.theme.SkeuoShadowDark
import com.example.ui.theme.TextDarkCocoa

@Composable
fun TvFrameContainer(
    frame: CastFrame?,
    status: CastStatus,
    pairingUrl: String,
    aspectRatioMode: AspectRatioMode,
    orientationConfig: OrientationConfig,
    onRotateClick: () -> Unit,
    onInvertClick: () -> Unit,
    onAspectClick: () -> Unit,
    onFullscreenClick: () -> Unit,
    onSnapshotClick: () -> Unit,
    onStartTestStream: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Bunny Ears Antenna on top
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.offset(y = 12.dp)
        ) {
            BunnyEar(tiltAngle = -15f)
            BunnyEar(tiltAngle = 15f)
        }

        // Retro TV Outer Shell
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(36.dp),
                    ambientColor = SkeuoShadowDark,
                    spotColor = SkeuoShadowDark
                )
                .clip(RoundedCornerShape(36.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFEEF2),
                            Color(0xFFFFDDE5),
                            Color(0xFFFFCCD7)
                        )
                    )
                )
                .border(4.dp, Color.White, RoundedCornerShape(36.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TV Screen Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(290.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(26.dp))
                        .clip(RoundedCornerShape(26.dp))
                        .background(SkeuoScreenBg)
                        .border(3.dp, Color(0xFFD6AAB4), RoundedCornerShape(26.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (frame?.bitmap != null && !frame.bitmap.isRecycled && status == CastStatus.STREAMING) {
                        val contentScale = when (aspectRatioMode) {
                            AspectRatioMode.FIT -> ContentScale.Fit
                            AspectRatioMode.FILL -> ContentScale.FillBounds
                            AspectRatioMode.ORIGINAL_16_9 -> ContentScale.Inside
                        }

                        Image(
                            bitmap = frame.bitmap.asImageBitmap(),
                            contentDescription = "同步画面",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationZ = orientationConfig.rotation.degrees.toFloat()
                                    scaleX = if (orientationConfig.isFlippedHorizontally) -1f else 1f
                                    scaleY = if (orientationConfig.isFlippedVertically) -1f else 1f
                                },
                            contentScale = contentScale
                        )

                        // Subtle scanline / gloss overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.10f),
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.05f)
                                        )
                                    )
                                )
                        )
                    } else {
                        // Standby Waiting State
                        StandbyScreenContent(
                            pairingUrl = pairingUrl,
                            onStartTestStream = onStartTestStream
                        )
                    }

                    // TV Channel / Status Badge (top-left)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x99000000))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (status == CastStatus.STREAMING) "CH-01 [LIVE]" else "CH-01 [STANDBY]",
                            color = if (status == CastStatus.STREAMING) MacaronMint else MacaronPink,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // TV Side Control Panel (Skeuomorphic dials & buttons)
                Column(
                    modifier = Modifier
                        .width(44.dp)
                        .padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dial 1: Rotate
                    TvControlDial(
                        icon = Icons.Default.CropRotate,
                        color = MacaronPink,
                        label = "${orientationConfig.rotation.degrees}°",
                        testTag = "tv_rotate_button",
                        onClick = onRotateClick
                    )

                    // Dial 2: Invert 180 (Fix upside down & mirrored)
                    TvControlDial(
                        icon = Icons.Default.Flip,
                        color = Color(0xFF6C5CE7),
                        label = "纠正",
                        testTag = "tv_invert_button",
                        onClick = onInvertClick
                    )

                    // Dial 3: Aspect Ratio
                    TvControlDial(
                        icon = Icons.Default.StayCurrentPortrait,
                        color = MacaronButter,
                        label = "比例",
                        testTag = "tv_aspect_button",
                        onClick = onAspectClick
                    )

                    // Dial 4: Fullscreen
                    TvControlDial(
                        icon = Icons.Default.Fullscreen,
                        color = MacaronSky,
                        label = "全屏",
                        testTag = "tv_fullscreen_button",
                        onClick = onFullscreenClick
                    )

                    // Dial 5: Snapshot
                    TvControlDial(
                        icon = Icons.Default.PhotoCamera,
                        color = MacaronMint,
                        label = "快照",
                        testTag = "tv_snapshot_button",
                        onClick = onSnapshotClick
                    )

                    SpeakerGrille()
                }
            }
        }
    }
}

@Composable
private fun BunnyEar(tiltAngle: Float) {
    Box(
        modifier = Modifier
            .size(width = 28.dp, height = 46.dp)
            .graphicsLayer { rotationZ = tiltAngle }
            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
            .background(Color(0xFFFFDDE5))
            .border(2.dp, Color.White, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 6.dp, bottomEnd = 6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 30.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFFFF8EA4).copy(alpha = 0.7f))
        )
    }
}

@Composable
private fun StandbyScreenContent(
    pairingUrl: String,
    onStartTestStream: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2B2D42), Color(0xFF1E212D))
                )
            )
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                QrCodeView(
                    data = pairingUrl,
                    size = 105.dp,
                    backgroundColor = Color.White
                )
                Text(
                    text = "扫码即刻同屏",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hero_pairing_1791942174399),
                    contentDescription = "萌宠同屏配对",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(14.dp))
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MacaronMint)
                        .clickable { onStartTestStream() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "▶ 极速自检",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "同一 WiFi 打开: $pairingUrl",
            color = Color(0xFFFFD166),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TvControlDial(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .testTag(testTag)
                .size(36.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color.White, color.copy(alpha = 0.3f), color)
                    )
                )
                .border(1.5.dp, Color.White, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = label,
            fontSize = 9.sp,
            color = TextDarkCocoa,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}

@Composable
private fun SpeakerGrille() {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 2.dp)
    ) {
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFBFA0A7))
                    )
                }
            }
        }
    }
}
