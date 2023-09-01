package edu.monash.smartcity.idledevicemanagement.task

import edu.monash.smartcity.idledevicemanagement.model.config.ApplianceConfig
import edu.monash.smartcity.idledevicemanagement.utils.generateCronTrigger
import org.springframework.scheduling.Trigger
import java.time.ZoneId

class ApplianceTurnOnTask(val applianceConfig: ApplianceConfig, timeZone: ZoneId): Runnable {
    val trigger: Trigger?

    init {
        trigger = if (applianceConfig.startTime != null) {
            generateCronTrigger(applianceConfig.startTime, timeZone)
        } else {
            null
        }
    }

    override fun run() {
        TODO("Not yet implemented")
    }
}