package edu.monash.smartcity.idledevicemanagement.model

data class IpAddressData(
    val timestampMilliseconds: Long,
    val sensorName: String,
    val ipAddress: String
)