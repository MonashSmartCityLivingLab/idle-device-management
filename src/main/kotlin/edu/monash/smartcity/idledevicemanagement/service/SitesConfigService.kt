package edu.monash.smartcity.idledevicemanagement.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import edu.monash.smartcity.idledevicemanagement.model.config.SiteConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class SitesConfigService(sitesConfigProperties: SitesConfigProperties) {
    final val sitesConfig: List<SiteConfig>


    init {
        logger.info { "Loading config from ${sitesConfigProperties.configPath.normalize().toAbsolutePath()}" }
        val objectMapper = jacksonObjectMapper()
        val file = sitesConfigProperties.configPath.toFile()
        sitesConfig = objectMapper.readValue(file)
        println()
    }
}