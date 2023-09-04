package edu.monash.smartcity.idledevicemanagement.model

data class OccupancyData(
    val timestampMilliseconds: Long,
    val sensorName: String,
    val occupied: Boolean
)