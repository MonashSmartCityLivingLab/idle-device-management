package edu.monash.smartcity.idledevicemanagement.model


open class ApplianceException: RuntimeException {
    constructor(): super()
    constructor(message: String): super(message)
    constructor(cause: Throwable): super(cause)
    constructor(message: String, cause: Throwable): super(message, cause)
}