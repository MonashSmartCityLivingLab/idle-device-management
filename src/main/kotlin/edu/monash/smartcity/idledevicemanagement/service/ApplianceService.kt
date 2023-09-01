package edu.monash.smartcity.idledevicemanagement.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import edu.monash.smartcity.idledevicemanagement.model.config.Appliance
import edu.monash.smartcity.idledevicemanagement.model.config.SiteConfig
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
                    pairs.add(appliance.deviceName to Appliance(appliance, ZoneId.of(site.timeZoneId)))
                }
            }
        }
        appliances = mapOf(*pairs.toTypedArray())
    }
}