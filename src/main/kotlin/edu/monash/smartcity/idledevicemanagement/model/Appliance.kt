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
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
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

    private fun checkPlugStatus() {
        if (!applianceConfig.recommendedForAutoOff) {
            return
        }
        val power = latestPower
        if (isRoomOccupied()) {
            addTurnOnTask()
        } else if (!isWithinStandardUseTime() && power != null && power < applianceConfig.standbyThreshold) { // if power is null, assume it's above threshold
            addTurnOffTask()
        }
    }

    private fun addTurnOffTask() {
        turnOnTaskFuture?.cancel(true)
        turnOnTaskFuture = null
        if (latestPlugStatus == true && (turnOffTaskFuture == null || turnOffTaskFuture?.isDone == true)) {
            getIpAddress()?.let { ipAddress ->
                val cutoffTime = Instant.now().plusSeconds(applianceConfig.cutoffWaitSeconds)
                logger.info { "Turning off appliance ${applianceConfig.deviceName} (${applianceConfig.sensorName}; ${getIpAddress()}) at ${OffsetDateTime.ofInstant(cutoffTime, ZoneOffset.UTC)}" }
                turnOffTaskFuture = scheduler.schedule(
                    ApplianceTurnOffTask(ipAddress),
                    cutoffTime
                )
            }
        }
    }

    private fun addTurnOnTask() {
        turnOffTaskFuture?.cancel(true)
        turnOffTaskFuture = null
        if ((latestPlugStatus == null || latestPlugStatus == false) && (turnOnTaskFuture == null || turnOnTaskFuture?.isDone == true)) {  // if plug status is null, assume it's off
            getIpAddress()?.let { ipAddress ->
                logger.info { "Turning on appliance ${applianceConfig.deviceName} (${applianceConfig.sensorName}; ${getIpAddress()})" }
                turnOnTaskFuture = scheduler.schedule(ApplianceTurnOnTask(ipAddress), Instant.now())
            }
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