package edu.monash.smartcity.idledevicemanagement.model

import edu.monash.smartcity.idledevicemanagement.model.config.ApplianceConfig
import edu.monash.smartcity.idledevicemanagement.model.config.MotionSensorConfig
import edu.monash.smartcity.idledevicemanagement.model.response.ApplianceLatestValues
import edu.monash.smartcity.idledevicemanagement.task.ApplianceTurnOffTask
import edu.monash.smartcity.idledevicemanagement.task.ApplianceTurnOnTask
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler
import java.net.InetAddress
import java.net.UnknownHostException
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.roundToLong
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

class Appliance(
    private val applianceConfig: ApplianceConfig,
    private val timeZone: ZoneId,
    motionSensorsConfig: List<MotionSensorConfig>,
    numberOfAppliancesInRoom: Int
) {
    private val scheduler: TaskScheduler = DefaultManagedTaskScheduler()
    private val motionSensors: Map<String, MotionSensor>
    private val maxTurnOnDelay = max(log2(numberOfAppliancesInRoom.toDouble()).roundToLong(), 1) * 1000

    private var turnOffTaskFuture: Future<*>? = null
    private var turnOnTaskFuture: Future<*>? = null

    private var latestIpAddress: InetAddress? = null
    private var latestPower: Double? = null
    private var latestPlugStatus: Boolean? = null

    private var overrideEnabled: Boolean = false
    private var overrideTimeout: ZonedDateTime? = null

    init {
        motionSensors = motionSensorsConfig.associate { sensor ->
            sensor.sensorName to MotionSensor(sensor.sensorName)
        }
    }

    fun getLatestValues(): ApplianceLatestValues {
        val overrideTimeoutSeconds = if (overrideEnabled && overrideTimeout != null) {
            ChronoUnit.SECONDS.between(Instant.now(), overrideTimeout)
        } else {
            null
        }
        return ApplianceLatestValues(
            applianceConfig.deviceName,
            applianceConfig.sensorName,
            latestPlugStatus,
            latestPower,
            overrideEnabled,
            overrideTimeoutSeconds
        )
    }

    fun updatePowerData(data: PowerData) {
        latestPower = data.power
        checkPlugSwitchTrigger()
    }

    fun updateOccupancyData(data: OccupancyData) {
        motionSensors[data.sensorName]?.latestOccupancy = data.occupied
        checkPlugSwitchTrigger()
    }

    fun updatePlugStatusData(data: PlugStatusData) {
        latestPlugStatus = data.isOn
    }

    fun updateIpAddress(data: IpAddressData) {
        latestIpAddress = InetAddress.getByName(data.ipAddress)
    }

    fun hasMotionSensorInRoom(deviceName: String) =
        motionSensors.keys.any { name -> name.lowercase() == deviceName.lowercase() }

    fun isOverride(): Boolean {
        return if (!overrideEnabled) {
            false
        } else {
            val now = getCurrentDateTime()
            overrideTimeout == null || now < overrideTimeout // if overrideTimeout is null, override is semi-permanent (i.e. until manually turned off)
        }
    }

    fun setOverride(enable: Boolean, durationSeconds: Long? = null) {
        overrideEnabled = enable
        overrideTimeout = if (durationSeconds != null) {
            ZonedDateTime.now(timeZone).plusSeconds(durationSeconds)
        } else {
            null
        }
    }


    fun turnOnNow() {
        val ipAddress = getIpAddress()
        if (ipAddress != null) {
            try {
                scheduler.schedule(ApplianceTurnOnTask(ipAddress), Instant.now()).get()
            } catch (e: ExecutionException) {
                throw ApplianceException("Cannot send turn on command to ${applianceConfig.deviceName} (${applianceConfig.sensorName}")
            }
        } else {
            throw ApplianceException("The IP address for this appliance's sensor is not yet known")
        }
    }

    fun turnOffNow() {
        val ipAddress = getIpAddress()
        if (ipAddress != null) {
            try {
                scheduler.schedule(ApplianceTurnOffTask(ipAddress), Instant.now()).get()
            } catch (e: ExecutionException) {
                throw ApplianceException("Cannot send turn off command to ${applianceConfig.deviceName} (${applianceConfig.sensorName}")
            }
        } else {
            throw ApplianceException("The IP address for this appliance's sensor is not yet known")
        }
    }

    private fun checkPlugSwitchTrigger() {
        if (!applianceConfig.recommendedForAutoOff || isOverride()) {
            // if appliance is not meant for auto-off or is being overridden, do not run check
            return
        }

        if (motionSensors.isNotEmpty()) { // trigger for plugs with motion sensors
            if (isRoomOccupied()) {
                addTurnOnTask()
            } else if (isPowerConsumptionBelowThreshold()) {
                addTurnOffTask()
            }
        } else {
            if (isWithinStandardUseTime()) {
                addTurnOnTask()
            } else if (isPowerConsumptionBelowThreshold()) {
                addTurnOffTask()
            }
        }
    }

    private fun addTurnOffTask() {
        turnOnTaskFuture?.cancel(false)
        turnOnTaskFuture = null
        if (isPlugTurnedOn() && (turnOffTaskFuture == null || turnOffTaskFuture?.isDone == true)) {
            getIpAddress()?.let { ipAddress ->
                val cutoffTime = Instant.now().plusSeconds(applianceConfig.cutoffWaitSeconds)
                logger.info {
                    "Turning off appliance ${applianceConfig.deviceName} (${applianceConfig.sensorName}; ${getIpAddress()}) at ${
                        OffsetDateTime.ofInstant(
                            cutoffTime,
                            ZoneOffset.UTC
                        )
                    }"
                }
                turnOffTaskFuture = scheduler.schedule(
                    ApplianceTurnOffTask(ipAddress),
                    cutoffTime
                )
            }
        }
    }

    private fun addTurnOnTask() {
        turnOffTaskFuture?.cancel(false)
        turnOffTaskFuture = null
        // Add random delay to prevent surges from all appliances turning on at once
        val randomDelay = Random.nextLong(0, maxTurnOnDelay)
        val time = Instant.now().plusMillis(randomDelay)
        if (isPlugTurnedOff() && (turnOnTaskFuture == null || turnOnTaskFuture?.isDone == true)) {
            getIpAddress()?.let { ipAddress ->
                logger.info {
                    "Turning on appliance ${applianceConfig.deviceName} (${applianceConfig.sensorName}; ${getIpAddress()}) at ${
                        OffsetDateTime.ofInstant(
                            time,
                            ZoneOffset.UTC
                        )
                    }"
                }
                turnOnTaskFuture = scheduler.schedule(ApplianceTurnOnTask(ipAddress), time)
            }
        }
    }

    private fun isRoomOccupied(): Boolean {
        // if ALL motion sensors hasn't got any data, assume that room is occupied
        return motionSensors.values.any { sensor -> sensor.latestOccupancy ?: false }
    }

    private fun isPlugTurnedOn() = latestPlugStatus == true

    private fun isPlugTurnedOff() =
        latestPlugStatus == null || latestPlugStatus == false  // if plug status is unknown, assume it's off

    private fun isPowerConsumptionBelowThreshold() =
        latestPower?.let { power -> power <= applianceConfig.standbyThreshold }
            ?: false // if power consumption is unknown, assume it's above threshold

    private fun isWithinStandardUseTime(): Boolean {
        val currentDateTime = getCurrentDateTime()
        val currentTime = currentDateTime.toLocalTime()
        val today = currentDateTime.dayOfWeek.value
        return applianceConfig.standardUseTimes.any { standardUseTime ->
            val start = LocalTime.parse(standardUseTime.startTime, DateTimeFormatter.ISO_LOCAL_TIME)
            val end = LocalTime.parse(standardUseTime.endTime, DateTimeFormatter.ISO_LOCAL_TIME)
            currentTime >= start && currentTime < end && today in standardUseTime.daysOfWeek
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

    private fun getCurrentDateTime() = ZonedDateTime.now(timeZone)
}