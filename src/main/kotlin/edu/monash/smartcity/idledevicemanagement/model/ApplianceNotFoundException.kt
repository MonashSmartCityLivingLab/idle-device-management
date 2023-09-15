package edu.monash.smartcity.idledevicemanagement.model


class ApplianceNotFoundException: ApplianceException {
    constructor(): super()
    constructor(message: String): super(message)
    constructor(cause: Throwable): super(cause)
    constructor(message: String, cause: Throwable): super(message, cause)
}