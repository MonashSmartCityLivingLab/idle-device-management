package edu.monash.smartcity.idledevicemanagement.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class SitesConfigProperties {
    @Value("\${idle-device-management.sites-config}")
    lateinit var configPath: Path
}