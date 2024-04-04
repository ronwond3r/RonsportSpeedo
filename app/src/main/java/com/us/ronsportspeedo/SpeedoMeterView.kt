package com.us.ronsportspeedo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.roundToInt


private var accelerometerListener: AccelerometerListener? = null
private var startTimeMillis by mutableStateOf(0L)
private var speedMessages by mutableStateOf("")

class SpeedoMeterViewModel : ViewModel() {
    private val _accelerometerListener = MutableLiveData<AccelerometerListener>()
    private val accelerometerListener: LiveData<AccelerometerListener> get() = _accelerometerListener

    private var isStartButtonPressed by mutableStateOf(false)

    private val _speedDataViewModel = SpeedDataViewModel()
    val speedData = _speedDataViewModel.speedData

    private var startTimeMillis: Long = 0L
    private var stopTimeMillis: Long = 0L

    init {
        // Initialize accelerometerListener here if needed
        // For example:
        _accelerometerListener.value = AccelerometerListener(
            onSpeedChange = { updateCurrentSpeed(it) },
            onTimeChange = { updateTime(it) }
        )
    }

    // ... Existing code ...

    // Function to update the speed data
    private fun updateCurrentSpeed(speed: Float) {
       // Log.d("SpeedoMeterUpdate", "Updated Speed: $speed")

        // Check speed every 5 seconds
        if (startTimeMillis == 0L) {
            //startTimeMillis = System.currentTimeMillis()
            handleSpeedLevel(speed)
        } else if (System.currentTimeMillis() - startTimeMillis >= 2000) {
            handleSpeedLevel(speed)
            speedMessages = handleSpeedLevel(speed)
        }

        accelerometerListener.value?.let {
            it.currentSpeed = ((speed * 100).roundToInt() / 100).toFloat()
        }

        // Calculate distance covered and update SpeedData
        val distanceCovered = calculateDistanceCovered(_accelerometerListener.value)
        updateSpeedData(
            SpeedData(
                currentSpeed = ((speed * 100).roundToInt() / 100).toFloat(),
                speedMessages = speedMessages,
                distanceCovered = distanceCovered
                // Add other necessary fields to SpeedData
            )
        )
    }



    fun updateTime(time: Long) {
       // Log.d("SpeedoMeterUpdate", "Updated Time: $time milliseconds")
        _speedDataViewModel.speedData.value.timeMillis
    }
    fun stopTime(currentTimeMillis: Long) {
        _speedDataViewModel.speedData.value.stopMillis
    }

    private fun handleSpeedLevel(speed: Float): String {
        return when {
            speed > 70 -> "Excellent!"
            speed > 50 -> "Keep up the good work!"
            speed > 10 -> "Add speed"
            else -> ""
        }
    }

  //  fun observeSpeedData(): Lifecycle.State<SpeedData> {
    //    return speedData }

    fun updateSpeedData(newSpeedData: SpeedData) {
        _speedDataViewModel.updateSpeedData(newSpeedData)
    }

    // Function to get the current accelerometerListener
    fun getAccelerometerListener(): AccelerometerListener? {
        return _accelerometerListener.value
    }

    // Function to set a new accelerometerListener
    fun setAccelerometerListener(listener: AccelerometerListener) {
        _accelerometerListener.value = listener
    }

    fun getCurrentSpeed(): Float {
        return accelerometerListener.value?.currentSpeed ?: 0.0f
    }

    fun updateStartButtonPressed(value: Boolean) {
        isStartButtonPressed = value
    }


}
