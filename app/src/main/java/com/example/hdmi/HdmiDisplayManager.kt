package com.example.hdmi

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import com.example.model.AspectRatioMode
import com.example.model.CastFrame
import com.example.model.HdmiOutputState
import com.example.model.OrientationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HdmiDisplayManager(
    private val context: Context,
    private val frameFlow: StateFlow<CastFrame?>,
    private val aspectRatioFlow: StateFlow<AspectRatioMode>,
    private val orientationFlow: StateFlow<OrientationConfig>? = null
) {
    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private val handler = Handler(Looper.getMainLooper())

    private val _hdmiState = MutableStateFlow(HdmiOutputState())
    val hdmiState: StateFlow<HdmiOutputState> = _hdmiState.asStateFlow()

    private var currentPresentation: MimiPresentation? = null
    private var activeSecondaryDisplay: Display? = null

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {
            Log.d("HdmiDisplayManager", "Display added: $displayId")
            checkForExternalDisplays()
        }

        override fun onDisplayRemoved(displayId: Int) {
            Log.d("HdmiDisplayManager", "Display removed: $displayId")
            if (activeSecondaryDisplay?.displayId == displayId) {
                stopPresentation()
                activeSecondaryDisplay = null
            }
            checkForExternalDisplays()
        }

        override fun onDisplayChanged(displayId: Int) {
            Log.d("HdmiDisplayManager", "Display changed: $displayId")
            checkForExternalDisplays()
        }
    }

    fun startListening() {
        displayManager.registerDisplayListener(displayListener, handler)
        checkForExternalDisplays()
    }

    fun stopListening() {
        displayManager.unregisterDisplayListener(displayListener)
        stopPresentation()
    }

    fun checkForExternalDisplays() {
        val presentationDisplays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
        val candidate = presentationDisplays.firstOrNull() ?: displayManager.displays.firstOrNull {
            it.displayId != Display.DEFAULT_DISPLAY
        }

        if (candidate != null) {
            activeSecondaryDisplay = candidate
            val metrics = DisplayMetrics()
            candidate.getRealMetrics(metrics)

            _hdmiState.value = HdmiOutputState(
                isConnected = true,
                displayId = candidate.displayId,
                displayName = candidate.name ?: "Type-C HDMI 外接显示设备",
                resolution = "${metrics.widthPixels} x ${metrics.heightPixels}",
                refreshRate = candidate.refreshRate,
                isPresenting = currentPresentation?.isShowing == true
            )

            // Auto-launch presentation if connected
            if (currentPresentation == null) {
                startPresentation(candidate)
            }
        } else {
            activeSecondaryDisplay = null
            _hdmiState.value = HdmiOutputState(isConnected = false)
            stopPresentation()
        }
    }

    fun togglePresentation() {
        if (currentPresentation?.isShowing == true) {
            stopPresentation()
        } else {
            activeSecondaryDisplay?.let { startPresentation(it) }
        }
    }

    private fun startPresentation(display: Display) {
        try {
            stopPresentation()
            val presentation = MimiPresentation(context, display, frameFlow, aspectRatioFlow, orientationFlow)
            presentation.show()
            currentPresentation = presentation
            _hdmiState.value = _hdmiState.value.copy(isPresenting = true)
            Log.d("HdmiDisplayManager", "HDMI Presentation started on ${display.name}")
        } catch (e: Exception) {
            Log.e("HdmiDisplayManager", "Failed to show presentation", e)
        }
    }

    fun stopPresentation() {
        try {
            currentPresentation?.dismiss()
        } catch (_: Exception) {}
        currentPresentation = null
        _hdmiState.value = _hdmiState.value.copy(isPresenting = false)
    }
}
