package edu.monash.smartcity.idledevicemanagement.model

data class PlugStatusData(
    val timestampMilliseconds: Long,
    val deviceName: String,
    val isOn: Boolean
)