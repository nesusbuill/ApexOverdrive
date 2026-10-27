package com.apex.overdrive.core

import kotlinx.coroutines.*

class ThermalGuard(
    private val monitor: SystemMonitor,
    private val onHighTemp: (Float, String) -> Unit
) {
    private var job: Job? = null
    private var lastTriggered = 0f

    fun start(scope: CoroutineScope) {
        job?.cancel()
        job = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val t = monitor.temp()
                    if (t > 0) {
                        if (t > 52f && (lastTriggered == 0f || t - lastTriggered > 2f)) {
                            onHighTemp(t, "balanced"); lastTriggered = t
                        } else if (t > 48f && t <= 52f &&
                            (lastTriggered == 0f || t - lastTriggered > 2f)) {
                            onHighTemp(t, "turbo"); lastTriggered = t
                        } else if (t < 42f) lastTriggered = 0f
                    }
                } catch (_: Throwable) {}
                delay(10000)
            }
        }
    }

    fun stop() { job?.cancel(); job = null }
}
