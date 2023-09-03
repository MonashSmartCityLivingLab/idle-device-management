package edu.monash.smartcity.idledevicemanagement.model

data class OccupancyData(
    val timestampMilliseconds: Long,
    val deviceName: String,
    val occupied: Boolean
)