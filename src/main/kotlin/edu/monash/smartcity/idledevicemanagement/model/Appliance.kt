package edu.monash.smartcity.idledevicemanagement.model

import edu.monash.smartcity.idledevicemanagement.model.config.ApplianceConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

class Appliance(val applianceConfig: ApplianceConfig, val timeZone: ZoneId) {
    private val scheduler: TaskScheduler = DefaultManagedTaskScheduler()
    private val startTime = LocalTime.parse(applianceConfig.startTime, DateTimeFormatter.ISO_LOCAL_TIME)
    private val endTime = LocalTime.parse(applianceConfig.endTime, DateTimeFormatter.ISO_LOCAL_TIME)

    private var latestPowerData: PowerData? = null
    private var latestOccupancyData: OccupancyData? = null
    private var latestPlugStatusData: PlugStatusData? = null

    fun updatePowerData(data: PowerData) {
        latestPowerData = data
        val time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(data.timestampMilliseconds), timeZone).toLocalTime()
        val currentTime = ZonedDateTime.now(timeZone).toLocalTime()
        if (latestOccupancyData?.occupied == false) { // if occupancy data is null, assume it's occupied to be safe
            if (currentTime >= time && currentTime < time) {
                if (latestPlugStatusData == null || latestPlugStatusData?.isOn == false) { // if plug status is null, assume it's off
                    logger.info { "Turning on appliance ${applianceConfig.deviceName} at $currentTime" }
                }
            } else if (data.power < applianceConfig.standbyThreshold) {
                logger.info { "Turning off appliance ${applianceConfig.deviceName} at $currentTime" }
            }
        }
    }

    fun updateOccupancyData(data: OccupancyData) {
        latestOccupancyData = data
        // TODO: check matching occupancy sensors
        val time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(data.timestampMilliseconds), timeZone).toLocalTime()
        val currentTime = ZonedDateTime.now(timeZone).toLocalTime()
        val power = latestPowerData?.power
        if (data.occupied) {
            if (latestPlugStatusData == null || latestPlugStatusData?.isOn == false) { // if plug status is null, assume it's off
                logger.info { "Turning on appliance ${applianceConfig.deviceName} at $currentTime" }
            }
        } else if (currentTime >= time && currentTime < time) {
            if (latestPlugStatusData == null || latestPlugStatusData?.isOn == false) { // if plug status is null, assume it's off
                logger.info { "Turning on appliance ${applianceConfig.deviceName} at $currentTime" }
            }
        } else if (power != null && power < applianceConfig.standbyThreshold) { // if power is null, assume it's above threshold
            logger.info { "Turning off appliance ${applianceConfig.deviceName} at $currentTime" }
        }
    }

    fun updatePlugStatusData(data: PlugStatusData) {
        latestPlugStatusData = data
    }

}