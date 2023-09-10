package edu.monash.smartcity.idledevicemanagement.model

import edu.monash.smartcity.idledevicemanagement.model.config.ApplianceConfig
import edu.monash.smartcity.idledevicemanagement.model.config.MotionSensorConfig
import edu.monash.smartcity.idledevicemanagement.task.ApplianceTurnOffTask
import edu.monash.smartcity.idledevicemanagement.task.ApplianceTurnOnTask
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler
import java.net.InetAddress
import java.net.UnknownHostException
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

    private var latestIpAddress: InetAddress? = null
    private var latestPower: Double? = null
    private var latestPlugStatus: Boolean? = null

    init {
        motionSensors = motionSensorsConfig.associate { sensor ->
            sensor.sensorName to MotionSensor(sensor.sensorName)
        }
    }

    fun updatePowerData(data: PowerData) {
        latestPower = data.power

        checkPlugStatus()
    }

    fun updateOccupancyData(data: OccupancyData) {
        motionSensors[data.sensorName]?.latestOccupancy = data.occupied

        checkPlugStatus()
    }

    fun updatePlugStatusData(data: PlugStatusData) {
        latestPlugStatus = data.isOn
    }

    fun updateIpAddress(data: IpAddressData) {
        latestIpAddress = InetAddress.getByName(data.ipAddress)
    }

    fun hasMotionSensorInRoom(deviceName: String) = motionSensors.keys.any { name -> name == deviceName }

    private fun isRoomOccupied(): Boolean {
        // if ALL motion sensors hasn't got any data, assume that room is occupied
        return motionSensors.values.all { sensor -> sensor.latestOccupancy == null }
                || motionSensors.values.any { sensor -> sensor.latestOccupancy ?: false }
    }

    private fun addTurnOffTask() {
        getIpAddress()?.let { ipAddress ->
            turnOnTaskFuture?.cancel(true)
            turnOnTaskFuture = null
            scheduler.schedule(
                ApplianceTurnOffTask(ipAddress),
                Instant.now().plusSeconds(applianceConfig.cutoffWaitSeconds)
            )
        }
    }

    private fun checkPlugStatus() {
        if (!applianceConfig.recommendedForAutoOff) {
            return
        }
        val power = latestPower
        if (isRoomOccupied()) {
            if (latestPlugStatus == null || latestPlugStatus == false) { // if plug status is null, assume it's off
                logger.info { "Turning on appliance ${applianceConfig.deviceName} (${applianceConfig.sensorName}; ${getIpAddress()}) due to occupancy" }
                addTurnOnTask()
            }
        } else if (!isWithinStandardUseTime() && power != null && power < applianceConfig.standbyThreshold && latestPlugStatus == true) { // if power is null, assume it's above threshold
            logger.info { "Turning off appliance ${applianceConfig.deviceName} (${applianceConfig.sensorName}; ${getIpAddress()}) due to idle" }
            addTurnOffTask()
        }
    }

    private fun addTurnOnTask() {
        getIpAddress()?.let { ipAddress ->
            turnOffTaskFuture?.cancel(true)
            turnOffTaskFuture = null
            scheduler.schedule(ApplianceTurnOnTask(ipAddress), Instant.now())
        }
    }

    private fun isWithinStandardUseTime(): Boolean {
        val currentTime = ZonedDateTime.now(timeZone).toLocalTime()
        return applianceConfig.standardUseTimes.any { standardUseTime ->
            val start = LocalTime.parse(standardUseTime.startTime, DateTimeFormatter.ISO_LOCAL_TIME)
            val end = LocalTime.parse(standardUseTime.endTime, DateTimeFormatter.ISO_LOCAL_TIME)
            currentTime >= start && currentTime < end
        }
    }

    private fun getIpAddress(): InetAddress? {
        return latestIpAddress ?: run {
            try {
                InetAddress.getByName("${applianceConfig.sensorName}.local")
            } catch (e: UnknownHostException) {
                null
            }
        }
    }
}