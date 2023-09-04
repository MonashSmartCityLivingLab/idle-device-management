package edu.monash.smartcity.idledevicemanagement.model

data class PlugStatusData(
    val timestampMilliseconds: Long,
    val sensorName: String,
    val isOn: Boolean
)