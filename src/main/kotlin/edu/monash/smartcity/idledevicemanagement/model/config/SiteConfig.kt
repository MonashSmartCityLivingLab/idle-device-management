package edu.monash.smartcity.idledevicemanagement.model.config

data class SiteConfig(
    val siteName: String,
    val timeZoneId: String,
    val rooms: List<RoomConfig>
)
