package edu.monash.smartcity.idledevicemanagement.model.response

data class ApplianceLatestValues(
    val deviceName: String,
    val sensorName: String,
    val isOn: Boolean?,
    val power: Double?,
    val overrideEnabled: Boolean,
    val overrideTimeoutSeconds: Long?
)