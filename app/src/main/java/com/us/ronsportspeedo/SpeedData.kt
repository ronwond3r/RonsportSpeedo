package com.us.ronsportspeedo

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel


data class SpeedData(
    val currentSpeed: Float = 0.0f,
    val speedMessages: String = "",
    val timeMillis: Long = 0,
    val  stopMillis: Long = 0,
    var isSpeedStart: Boolean = false,
    var isStartButtonPressed: Boolean = false,
    var distanceCovered: Float = 0.0f // Add distanceCovered field
    // Add other necessary fields
)

class SpeedDataViewModel : ViewModel() {
    private val _speedData = mutableStateOf(SpeedData())
    val speedData
        get() = _speedData

    fun updateSpeedData(newSpeedData: SpeedData) {
        _speedData.value = newSpeedData
    }
}


