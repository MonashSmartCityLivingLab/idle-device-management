package edu.monash.smartcity.idledevicemanagement.model.config

import edu.monash.smartcity.idledevicemanagement.task.ApplianceTurnOffTask
import edu.monash.smartcity.idledevicemanagement.task.ApplianceTurnOnTask
import java.time.ZoneId
import java.time.ZonedDateTime

class Appliance(val applianceConfig: ApplianceConfig, val timeZone: ZoneId) {
    var latestPowerReading: Double? = null
    var latestPowerReadingTime: ZonedDateTime? = null
    val turnOnTask: ApplianceTurnOnTask = ApplianceTurnOnTask(applianceConfig, timeZone)
    val turnOffTask = ApplianceTurnOffTask(applianceConfig, timeZone)
    fun turnOn() {
        turnOnTask.run()
    }

    fun turnOff() {
        turnOffTask.run()
    }
}