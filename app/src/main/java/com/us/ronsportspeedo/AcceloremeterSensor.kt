package com.us.ronsportspeedo

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Build
import androidx.annotation.RequiresApi
import kotlin.math.roundToInt


class AccelerometerListener(
    private val onSpeedChange: (Float) -> Unit,
    private val onTimeChange: (Long) -> Unit
) : SensorEventListener {
    private var lastTimestamp: Long = 0L
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var lastZ: Float = 0f
    private var initialAcceleration: Float = 0f
    internal var currentSpeed: Float = (0f.roundToInt() / 10).toFloat()
    private var startTimeMillis: Long = 0
    internal var isSpeedMeasurementStarted = false

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this example
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            if (lastTimestamp != 0L) {
                val timeDelta = (currentTime - lastTimestamp) / 1000.0f // Convert to seconds
                val x = event.values[0].roundToInt()
                val y = event.values[1].roundToInt()
                val z = event.values[2].roundToInt()

                // Calculate acceleration using the accelerometer data
                val acceleration = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                // Initialize initialAcceleration if it hasn't been initialized before
                if (initialAcceleration == 0f) {
                    initialAcceleration = acceleration
                } else {
                    // Subtract changes to obtain velocity only if the current acceleration is greater
                    currentSpeed = if (acceleration > initialAcceleration) {
                        acceleration - initialAcceleration
                    } else {
                        0f
                    }
                }

                // Notify the callback about the speed change
                onSpeedChange(currentSpeed)
            }
            lastTimestamp = currentTime
            lastX = event.values[0]
            lastY = event.values[1]
            lastZ = event.values[2]

            if (isSpeedMeasurementStarted) {
                // Notify the callback about the time change
                onTimeChange(currentTime - startTimeMillis)
            }
        }
    }

    fun reset() {
        lastTimestamp = 0L
        lastX = 0f
        lastY = 0f
        lastZ = 0f
        currentSpeed = (0f.roundToInt() / 10).toFloat()
        initialAcceleration = 0f
        startTimeMillis = 0
        isSpeedMeasurementStarted = false
    }

    fun getCurrentTime(): Long {
        return System.currentTimeMillis()
    }
}






