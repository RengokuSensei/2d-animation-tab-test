package com.example.frameforge.cinema

import com.example.frameforge.model.DrawingStroke
import com.example.frameforge.model.StrokePoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AutoAdvanceMode(val label: String, val description: String) {
    STROKE_LIFT_DEBOUNCE("Stroke-Lift Pause", "Auto-advances when stylus is lifted after a pause threshold"),
    LIVE_LOOP_FLIPBOOK("Live Loop Playback", "Loops timeline while drawing; strokes stamp into frame under playhead"),
    SMART_MOMENTUM_CLONE("Smart Momentum Clone", "Advances and clones previous pose shifted along stylus velocity")
}

data class AutoAdvanceState(
    val isEnabled: Boolean = false,
    val mode: AutoAdvanceMode = AutoAdvanceMode.STROKE_LIFT_DEBOUNCE,
    val debounceMs: Long = 600L, // 0L (instant), 250L (fast sketch), 600L (multi-stroke), 1200L (detailed)
    val liveLoopFps: Int = 8,    // 4, 8, 12 FPS
    val momentumShift: Boolean = true,
    val countdownProgress: Float = 0f // 0..1 for radial cursor countdown
)

class AutoAdvanceFlipbookController(
    private val coroutineScope: CoroutineScope,
    private val onTriggerAdvance: (velocityShiftX: Float, velocityShiftY: Float) -> Unit
) {
    private val _state = MutableStateFlow(AutoAdvanceState())
    val state: StateFlow<AutoAdvanceState> = _state.asStateFlow()

    private var debounceJob: Job? = null
    private var lastStrokeVelocityX = 0f
    private var lastStrokeVelocityY = 0f

    fun toggleAutoAdvance() {
        _state.update { it.copy(isEnabled = !it.isEnabled) }
        if (!_state.value.isEnabled) {
            cancelPendingAdvance()
        }
    }

    fun setMode(mode: AutoAdvanceMode) {
        _state.update { it.copy(mode = mode) }
    }

    fun setDebounceThreshold(ms: Long) {
        _state.update { it.copy(debounceMs = ms) }
    }

    fun setLiveLoopFps(fps: Int) {
        _state.update { it.copy(liveLoopFps = fps) }
    }

    fun onStrokeStarted() {
        // Cancel pending advance if user starts drawing another stroke quickly
        cancelPendingAdvance()
    }

    fun onStrokeCompleted(stroke: DrawingStroke) {
        if (!_state.value.isEnabled) return

        // Compute velocity from the last points of the stroke
        val pts = stroke.points
        if (pts.size >= 2) {
            val pEnd = pts.last()
            val pPrev = pts[pts.size - 2]
            lastStrokeVelocityX = (pEnd.x - pPrev.x).coerceIn(-60f, 60f)
            lastStrokeVelocityY = (pEnd.y - pPrev.y).coerceIn(-60f, 60f)
        } else {
            lastStrokeVelocityX = 0f
            lastStrokeVelocityY = 0f
        }

        when (_state.value.mode) {
            AutoAdvanceMode.STROKE_LIFT_DEBOUNCE -> {
                val delayMs = _state.value.debounceMs
                if (delayMs <= 0L) {
                    onTriggerAdvance(0f, 0f)
                } else {
                    startCountdownTimer(delayMs, 0f, 0f)
                }
            }
            AutoAdvanceMode.SMART_MOMENTUM_CLONE -> {
                startCountdownTimer(
                    _state.value.debounceMs,
                    lastStrokeVelocityX,
                    lastStrokeVelocityY
                )
            }
            AutoAdvanceMode.LIVE_LOOP_FLIPBOOK -> {
                // In live loop mode, playback manages frame progression continuously
            }
        }
    }

    private fun startCountdownTimer(totalMs: Long, velX: Float, velY: Float) {
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            val steps = 12
            val stepDelay = totalMs / steps
            for (step in 1..steps) {
                delay(stepDelay)
                _state.update { it.copy(countdownProgress = step.toFloat() / steps) }
            }
            _state.update { it.copy(countdownProgress = 0f) }
            onTriggerAdvance(velX, velY)
        }
    }

    fun cancelPendingAdvance() {
        debounceJob?.cancel()
        debounceJob = null
        _state.update { it.copy(countdownProgress = 0f) }
    }
}
