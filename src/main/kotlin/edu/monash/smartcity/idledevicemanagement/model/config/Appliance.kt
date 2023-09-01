package edu.monash.smartcity.idledevicemanagement.model.config

import edu.monash.smartcity.idledevicemanagement.task.ApplianceTurnOnTask
import org.springframework.scheduling.support.CronTrigger
import java.time.ZoneId
import java.time.ZonedDateTime

class Appliance(val applianceConfig: ApplianceConfig, val timeZone: ZoneId) {
    var latestPowerReading: Double? = null
    var latestPowerReadingTime: ZonedDateTime? = null
    val turnOnTask: ApplianceTurnOnTask = ApplianceTurnOnTask(applianceConfig, timeZone)

    fun turnOn() {
        turnOnTask.run()
    }
}