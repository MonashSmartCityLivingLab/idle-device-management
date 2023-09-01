package edu.monash.smartcity.idledevicemanagement.task

import edu.monash.smartcity.idledevicemanagement.model.config.ApplianceConfig
import edu.monash.smartcity.idledevicemanagement.utils.generateCronTrigger
import org.springframework.scheduling.Trigger
import java.time.ZoneId

class ApplianceTurnOffTask(val applianceConfig: ApplianceConfig, timeZone: ZoneId): Runnable {
    val trigger: Trigger?

    init {
        trigger = if (applianceConfig.endTime != null) {
            generateCronTrigger(applianceConfig.endTime, timeZone)
        } else {
            null
        }
    }

    override fun run() {
        TODO("Not yet implemented")
    }
}