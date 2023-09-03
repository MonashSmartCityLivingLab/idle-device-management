package edu.monash.smartcity.idledevicemanagement.model

data class PowerData(
    val timestampMilliseconds: Long,
    val deviceName: String,
    val power: Double
)