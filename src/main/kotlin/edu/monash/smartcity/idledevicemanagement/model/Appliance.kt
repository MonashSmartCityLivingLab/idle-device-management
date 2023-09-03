package edu.monash.smartcity.idledevicemanagement.model

import edu.monash.smartcity.idledevicemanagement.model.config.ApplianceConfig
import edu.monash.smartcity.idledevicemanagement.model.config.MotionSensorConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

class Appliance(
    val applianceConfig: ApplianceConfig,
    val timeZone: ZoneId,
    motionSensorsConfig: List<MotionSensorConfig>
) {
    private val scheduler: TaskScheduler = DefaultManagedTaskScheduler()
    private val startTime = LocalTime.parse(applianceConfig.startTime, DateTimeFormatter.ISO_LOCAL_TIME)
    private val endTime = LocalTime.parse(applianceConfig.endTime, DateTimeFormatter.ISO_LOCAL_TIME)
    private val motionSensors: Map<String, MotionSensor>

    private var latestPowerData: PowerData? = null
    private var latestPlugStatusData: PlugStatusData? = null

    init {
        motionSensors = motionSensorsConfig.associate { sensor ->
            sensor.sensorName to MotionSensor(sensor.sensorName)
        }
    }

    fun updatePowerData(data: PowerData) {
        latestPowerData = data
        val time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(data.timestampMilliseconds), timeZone).toLocalTime()
        val currentTime = ZonedDateTime.now(timeZone).toLocalTime()
        if (isRoomOccupied()) {
            if (currentTime >= time && currentTime < time) {
                if (latestPlugStatusData == null || latestPlugStatusData?.isOn == false) { // if plug status is null, assume it's off
                    logger.info { "Turning on appliance ${applianceConfig.deviceName} at $currentTime due to standard use time" }
                }
            } else if (data.power < applianceConfig.standbyThreshold) {
                logger.info { "Turning off appliance ${applianceConfig.deviceName} at $currentTime due to idle" }
            }
        }
    }

    fun updateOccupancyData(data: OccupancyData) {
        motionSensors[data.deviceName]?.latestOccupancyData = data
        // TODO: check matching occupancy sensors
        val time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(data.timestampMilliseconds), timeZone).toLocalTime()
        val currentTime = ZonedDateTime.now(timeZone).toLocalTime()
        val power = latestPowerData?.power
        if (isRoomOccupied()) {
            if (latestPlugStatusData == null || latestPlugStatusData?.isOn == false) { // if plug status is null, assume it's off
                logger.info { "Turning on appliance ${applianceConfig.deviceName} at $currentTime due to occupancy" }
            }
        } else if (currentTime >= time && currentTime < time) {
            if (latestPlugStatusData == null || latestPlugStatusData?.isOn == false) { // if plug status is null, assume it's off
                logger.info { "Turning on appliance ${applianceConfig.deviceName} at $currentTime due to standard use time" }
            }
        } else if (power != null && power < applianceConfig.standbyThreshold) { // if power is null, assume it's above threshold
            logger.info { "Turning off appliance ${applianceConfig.deviceName} at $currentTime due to idle" }
        }
    }

    fun updatePlugStatusData(data: PlugStatusData) {
        latestPlugStatusData = data
    }

    private fun isRoomOccupied(): Boolean {
        // if ALL motion sensors hasn't got any data, assume that room is occupied
        return motionSensors.values.all { sensor -> sensor.latestOccupancyData == null }
                || motionSensors.values.any { sensor -> sensor.latestOccupancyData?.occupied ?: false }
    }
}