package edu.monash.smartcity.idledevicemanagement

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class IdleDeviceManagementApplication

fun main(args: Array<String>) {
    runApplication<IdleDeviceManagementApplication>(*args)
}
