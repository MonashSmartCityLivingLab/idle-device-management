package edu.monash.smartcity.idledevicemanagement.model

import edu.monash.smartcity.idledevicemanagement.model.config.ApplianceConfig
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler
import java.time.ZoneId

class Appliance(val applianceConfig: ApplianceConfig, val timeZone: ZoneId) {
    private val scheduler: TaskScheduler = DefaultManagedTaskScheduler()
    private var latestPowerData: PowerData? = null
    private var latestOccupancyData: OccupancyData? = null

    fun updatePowerData(data: PowerData) {
        TODO()
    }

    fun updateOccupancyData(data: OccupancyData) {
        TODO()
    }
}