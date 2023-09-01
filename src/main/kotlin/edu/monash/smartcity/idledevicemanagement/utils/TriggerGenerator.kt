package edu.monash.smartcity.idledevicemanagement.utils

import org.springframework.scheduling.support.CronTrigger
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun generateCronTrigger(time: String, timeZone: ZoneId): CronTrigger {
    val localTime = LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME)
    val minute = localTime.minute
    val hour = localTime.hour
    return CronTrigger("0 $minute $hour * * *", timeZone)
}