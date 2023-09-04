package edu.monash.smartcity.idledevicemanagement.model

data class PowerData(
    val timestampMilliseconds: Long,
    val sensorName: String,
    val power: Double
)