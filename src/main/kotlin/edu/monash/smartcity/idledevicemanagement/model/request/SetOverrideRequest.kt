package edu.monash.smartcity.idledevicemanagement.model.request

data class SetOverrideRequest(
    val enable: Boolean,
    val durationSeconds: Long?
)
