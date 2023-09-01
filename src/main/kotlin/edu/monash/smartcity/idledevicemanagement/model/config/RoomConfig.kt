package edu.monash.smartcity.idledevicemanagement.model.config

data class RoomConfig(
    val roomName: String,
    val appliances: List<ApplianceConfig>,
    val motionSensors: List<MotionSensorConfig>
)
