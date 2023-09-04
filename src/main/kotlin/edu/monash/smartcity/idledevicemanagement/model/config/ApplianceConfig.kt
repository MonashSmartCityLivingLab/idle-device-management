package edu.monash.smartcity.idledevicemanagement.model.config

data class ApplianceConfig(
    val deviceName: String,
    val sensorName: String,
    val standbyThreshold: Double,
    val recommendedForAutoOff: Boolean,
    val cutoffWaitSeconds: Long,
    val onTimeoutSeconds: Long,
    val startTime: String?,
    val endTime: String?
)
