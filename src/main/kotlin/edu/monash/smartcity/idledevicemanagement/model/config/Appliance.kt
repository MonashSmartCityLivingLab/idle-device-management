package edu.monash.smartcity.idledevicemanagement.model.config

import edu.monash.smartcity.idledevicemanagement.task.ApplianceTurnOffTask
import edu.monash.smartcity.idledevicemanagement.task.ApplianceTurnOnTask
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.Future

class Appliance(val applianceConfig: ApplianceConfig, val timeZone: ZoneId) {
    private var latestPowerReading: Double? = null
    private var latestPowerReadingTime: ZonedDateTime? = null
    private val turnOnTask: ApplianceTurnOnTask = ApplianceTurnOnTask(applianceConfig, timeZone)
    private val turnOffTask: ApplianceTurnOffTask = ApplianceTurnOffTask(applianceConfig, timeZone)
    private val scheduler: TaskScheduler = DefaultManagedTaskScheduler()
    private val scheduledTurnOnFuture: Future<*>? = if (turnOnTask.trigger != null) {
        scheduler.schedule(turnOnTask, turnOnTask.trigger)
    } else {
        null
    }
    private val scheduledTurnOffFuture: Future<*>? = if (turnOffTask.trigger != null) {
        scheduler.schedule(turnOffTask, turnOffTask.trigger)
    } else {
        null
    }
}