package edu.monash.smartcity.idledevicemanagement.model

import edu.monash.smartcity.idledevicemanagement.model.config.ApplianceConfig
import edu.monash.smartcity.idledevicemanagement.model.config.MotionSensorConfig
import edu.monash.smartcity.idledevicemanagement.task.ApplianceTurnOffTask
import edu.monash.smartcity.idledevicemanagement.task.ApplianceTurnOnTask
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler
import java.net.InetAddress
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Future

private val logger = KotlinLogging.logger {}

class Appliance(
    private val applianceConfig: ApplianceConfig,
    private val timeZone: ZoneId,
    motionSensorsConfig: List<MotionSensorConfig>
) {
    private val scheduler: TaskScheduler = DefaultManagedTaskScheduler()
    private val motionSensors: Map<String, MotionSensor>

    var turnOffTaskFuture: Future<*>? = null
    var turnOnTaskFuture: Future<*>? = null

    private var latestIpAddressData: IpAddressData? = null
    private var latestPowerData: PowerData? = null
    private var latestPlugStatusData: PlugStatusData? = null

    init {
        motionSensors = motionSensorsConfig.associate { sensor ->
            sensor.sensorName to MotionSensor(sensor.sensorName)
        }
    }

    fun updatePowerData(data: PowerData) {
        latestPowerData = data

        if (!applianceConfig.recommendedForAutoOff) { return }
        val currentTime = ZonedDateTime.now(timeZone).toLocalTime()
        if (!isRoomOccupied()) {
            if (isWithinStandardUseTime()) {
                if (latestPlugStatusData == null || latestPlugStatusData?.isOn == false) { // if plug status is null, assume it's off
                    logger.info { "Turning on appliance ${applianceConfig.deviceName} at $currentTime due to standard use time" }
                    addTurnOnTask()
                }
            } else if (data.power < applianceConfig.standbyThreshold && latestPlugStatusData?.isOn == true) {
                logger.info { "Turning off appliance ${applianceConfig.deviceName} at $currentTime due to idle" }
                addTurnOffTask()
            }
        }
    }

    fun updateOccupancyData(data: OccupancyData) {
        motionSensors[data.sensorName]?.latestOccupancyData = data

        if (!applianceConfig.recommendedForAutoOff) { return }
        val currentTime = ZonedDateTime.now(timeZone).toLocalTime()
        val power = latestPowerData?.power
        if (isRoomOccupied()) {
            if (latestPlugStatusData == null || latestPlugStatusData?.isOn == false) { // if plug status is null, assume it's off
                logger.info { "Turning on appliance ${applianceConfig.deviceName} at $currentTime due to occupancy" }
                addTurnOnTask()
            }
        } else if (isWithinStandardUseTime()) {
            if (latestPlugStatusData == null || latestPlugStatusData?.isOn == false) { // if plug status is null, assume it's off
                logger.info { "Turning on appliance ${applianceConfig.deviceName} at $currentTime due to standard use time" }
                addTurnOnTask()
            }
        } else if (power != null && power < applianceConfig.standbyThreshold && latestPlugStatusData?.isOn == true) { // if power is null, assume it's above threshold
            logger.info { "Turning off appliance ${applianceConfig.deviceName} at $currentTime due to idle" }
            addTurnOffTask()
        }
    }

    fun updatePlugStatusData(data: PlugStatusData) {
        latestPlugStatusData = data
    }

    fun updateIpAddress(data: IpAddressData) {
        latestIpAddressData = data
    }

    fun hasMotionSensorInRoom(deviceName: String) = motionSensors.keys.any { name -> name == deviceName }

    private fun isRoomOccupied(): Boolean {
        // if ALL motion sensors hasn't got any data, assume that room is occupied
        return motionSensors.values.all { sensor -> sensor.latestOccupancyData == null }
                || motionSensors.values.any { sensor -> sensor.latestOccupancyData?.occupied ?: false }
    }

    private fun addTurnOffTask() {
        latestIpAddressData?.ipAddress.let { ipAddress ->
            turnOnTaskFuture?.cancel(true)
            turnOnTaskFuture = null
            scheduler.schedule(ApplianceTurnOffTask(InetAddress.getByName(ipAddress)), Instant.now().plusSeconds(applianceConfig.cutoffWaitSeconds))
        }
    }

    private fun addTurnOnTask() {
        latestIpAddressData?.ipAddress.let { ipAddress ->
            turnOffTaskFuture?.cancel(true)
            turnOffTaskFuture = null
            scheduler.schedule(ApplianceTurnOnTask(InetAddress.getByName(ipAddress)), Instant.now())
        }
    }

    private fun isWithinStandardUseTime(): Boolean {
        val currentTime = ZonedDateTime.now(timeZone).toLocalTime()
        return applianceConfig.standardUseTimes.any {standardUseTime ->
            val start = LocalTime.parse(standardUseTime.startTime, DateTimeFormatter.ISO_LOCAL_TIME)
            val end = LocalTime.parse(standardUseTime.endTime, DateTimeFormatter.ISO_LOCAL_TIME)
            currentTime >= start && currentTime < end
        }
    }
}