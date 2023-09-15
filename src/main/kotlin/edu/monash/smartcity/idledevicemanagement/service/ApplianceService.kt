package edu.monash.smartcity.idledevicemanagement.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import edu.monash.smartcity.idledevicemanagement.model.*
import edu.monash.smartcity.idledevicemanagement.model.config.SiteConfig
import edu.monash.smartcity.idledevicemanagement.model.request.SetOverrideRequest
import edu.monash.smartcity.idledevicemanagement.model.response.ApplianceLatestValues
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.ZoneId

private val logger = KotlinLogging.logger {}

@Service
class ApplianceService(sitesConfigProperties: SitesConfigProperties) {
    final val sitesConfig: List<SiteConfig>
    final val appliances: Map<String, Appliance>


    init {
        logger.info { "Loading config from ${sitesConfigProperties.configPath.normalize().toAbsolutePath()}" }
        val objectMapper = jacksonObjectMapper()
        val file = sitesConfigProperties.configPath.toFile()
        sitesConfig = objectMapper.readValue(file)

        // set up appliances
        val pairs: MutableList<Pair<String, Appliance>> = mutableListOf()
        for (site in sitesConfig) {
            for (room in site.rooms) {
                for (appliance in room.appliances) {
                    pairs.add(
                        appliance.sensorName to Appliance(
                            appliance,
                            ZoneId.of(site.timeZoneId),
                            room.motionSensors
                        )
                    )
                }
            }
        }
        appliances = pairs.toMap()
    }

    fun updatePowerData(data: PowerData) {
        appliances[data.sensorName]?.updatePowerData(data)
    }

    fun updateOccupancyData(data: OccupancyData) {
        appliances.values.filter { appliance ->
            appliance.hasMotionSensorInRoom(data.sensorName)
        }.forEach { appliance ->
            appliance.updateOccupancyData(data)
        }
    }

    fun updatePlugStatusData(data: PlugStatusData) {
        appliances[data.sensorName]?.updatePlugStatusData(data)
    }

    fun updateIpAddress(data: IpAddressData) {
        appliances[data.sensorName]?.updateIpAddress(data)
    }

    fun turnOnApplianceNow(sensorName: String) {
        val appliance = appliances[sensorName]
        if (appliance != null) {
            appliance.turnOnNow()
        } else {
            throw ApplianceNotFoundException("No such appliance with sensor name $sensorName")
        }
    }

    fun turnOffApplianceNow(sensorName: String) {
        val appliance = appliances[sensorName]
        if (appliance != null) {
            appliance.turnOffNow()
        } else {
            throw ApplianceNotFoundException("No such appliance with sensor name $sensorName")
        }
    }

    fun setOverride(sensorName: String, payload: SetOverrideRequest) {
        val appliance = appliances[sensorName]
        if (appliance != null) {
            appliance.setOverride(payload.enable, payload.durationSeconds)
        } else {
            throw ApplianceNotFoundException("No such appliance with sensor name $sensorName")
        }
    }

    fun getLatestValues(sensorName: String): ApplianceLatestValues {
        val appliance = appliances[sensorName]
        if (appliance != null) {
            return appliance.getLatestValues()
        } else {
            throw ApplianceNotFoundException("No such appliance with sensor name $sensorName")
        }
    }
}