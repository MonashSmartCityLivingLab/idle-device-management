package edu.monash.smartcity.idledevicemanagement.model.config

data class ApplianceConfig(
    val deviceName: String,
    val sensorName: String,
    val standbyThreshold: Double,
    val recommendedForAutoOff: Boolean,
    val cutoffWaitSeconds: Long,
    val standardUseTimes: List<StandardUseTime>
)
