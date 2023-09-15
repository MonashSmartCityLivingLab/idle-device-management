package edu.monash.smartcity.idledevicemanagement.controller

import edu.monash.smartcity.idledevicemanagement.model.response.ErrorResponse
import com.fasterxml.jackson.core.JsonProcessingException
import edu.monash.smartcity.idledevicemanagement.model.ApplianceException
import edu.monash.smartcity.idledevicemanagement.model.ApplianceNotFoundException
import edu.monash.smartcity.idledevicemanagement.model.request.SetOverrideRequest
import edu.monash.smartcity.idledevicemanagement.service.ApplianceService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody

private val logger = KotlinLogging.logger {}

@Controller
class WebInterfaceController(val applianceService: ApplianceService) {
    @PostMapping("/sensor/{sensorName:.+}/turn-on")
    @ResponseBody
    fun turnOnApplianceNow(@PathVariable sensorName: String) {
        applianceService.turnOnApplianceNow(sensorName)
    }

    @PostMapping("/sensor/{sensorName:.+}/turn-off")
    @ResponseBody
    fun turnOffApplianceNow(@PathVariable sensorName: String) {
        applianceService.turnOffApplianceNow(sensorName)
    }
    @PostMapping("/sensor/{sensorName:.+}/override")
    @ResponseBody
    fun setOverride(@PathVariable sensorName: String, @RequestBody body: SetOverrideRequest) {
        applianceService.setOverride(sensorName, body)
    }


    @ExceptionHandler(ApplianceNotFoundException::class)
    @ResponseBody
    fun handleApplianceNotFoundException(e: ApplianceNotFoundException) = ResponseEntity(
        ErrorResponse(e.message ?: "No such appliance with the specified sensor name"),
        HttpStatus.INTERNAL_SERVER_ERROR
    )

    @ExceptionHandler(ApplianceException::class)
    @ResponseBody
    fun handleApplianceException(e: ApplianceException) = ResponseEntity(
        ErrorResponse(e.message ?: "An error occurred while performing an operation to the plug"),
        HttpStatus.INTERNAL_SERVER_ERROR
    )

    @ExceptionHandler(JsonProcessingException::class)
    @ResponseBody
    fun handleJsonError(e: JsonProcessingException): ResponseEntity<ErrorResponse> {
        logger.error(e) { "Invalid JSON received" }
        return ResponseEntity(ErrorResponse("Invalid JSON received."), HttpStatus.BAD_REQUEST)
    }
}