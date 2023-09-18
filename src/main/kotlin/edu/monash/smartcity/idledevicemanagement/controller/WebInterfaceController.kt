package edu.monash.smartcity.idledevicemanagement.controller

import com.fasterxml.jackson.core.JsonProcessingException
import edu.monash.smartcity.idledevicemanagement.model.ApplianceException
import edu.monash.smartcity.idledevicemanagement.model.ApplianceNotFoundException
import edu.monash.smartcity.idledevicemanagement.model.request.SetOverrideRequest
import edu.monash.smartcity.idledevicemanagement.model.response.ApplianceLatestValues
import edu.monash.smartcity.idledevicemanagement.model.response.ErrorResponse
import edu.monash.smartcity.idledevicemanagement.service.ApplianceService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@Controller
class WebInterfaceController(val applianceService: ApplianceService) {
    @Operation(summary = "Get latest values of a sensor.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(
                responseCode = "404",
                description = "No such appliance with the sensor name",
                content = [Content(schema = Schema(implementation = ErrorResponse::class), mediaType = "application/json")]
            )
        ]
    )
    @GetMapping("/sensor/{sensorName:.+}/latest-values", produces = ["application/json"])
    @ResponseBody
    fun getLatestValues(@PathVariable sensorName: String): ApplianceLatestValues {
        return applianceService.getLatestValues(sensorName)
    }

    @Operation(summary = "Get latest values of all sensors.")
    @GetMapping("/latest-values", produces = ["application/json"])
    @ResponseBody
    fun getAllLatestValues() = applianceService.getAllLatestValues()

    @Operation(summary = "Turn on an appliance.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(
                responseCode = "404",
                description = "No such appliance with the sensor name",
                content = [Content(schema = Schema(implementation = ErrorResponse::class), mediaType = "application/json")]
            ),
            ApiResponse(
                responseCode = "500",
                description = "An error occurred while sending a command to the sensor",
                content = [Content(schema = Schema(implementation = ErrorResponse::class), mediaType = "application/json")]
            )
        ]
    )
    @PostMapping("/sensor/{sensorName:.+}/turn-on")
    @ResponseBody
    fun turnOnApplianceNow(@PathVariable sensorName: String) {
        applianceService.turnOnApplianceNow(sensorName)
    }

    @Operation(summary = "Turn off an appliance.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(
                responseCode = "404",
                description = "No such appliance with the sensor name",
                content = [Content(schema = Schema(implementation = ErrorResponse::class), mediaType = "application/json")]
            ),
            ApiResponse(
                responseCode = "500",
                description = "An error occurred while sending a command to the sensor",
                content = [Content(schema = Schema(implementation = ErrorResponse::class), mediaType = "application/json")]
            )
        ]
    )
    @PostMapping("/sensor/{sensorName:.+}/turn-off")
    @ResponseBody
    fun turnOffApplianceNow(@PathVariable sensorName: String) {
        applianceService.turnOffApplianceNow(sensorName)
    }

    @Operation(summary = "Override auto on/off operations for a specified period of time (or until manually disabled).")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(
                responseCode = "404",
                description = "No such appliance with the sensor name",
                content = [Content(schema = Schema(implementation = ErrorResponse::class), mediaType = "application/json")]
            )
        ]
    )
    @PostMapping("/sensor/{sensorName:.+}/override", consumes = ["application/json"])
    @ResponseBody
    fun setOverride(@PathVariable sensorName: String, @RequestBody body: SetOverrideRequest) {
        applianceService.setOverride(sensorName, body)
    }

    @GetMapping("/config", produces = ["application/json"])
    @ResponseBody
    fun getConfig() = applianceService.sitesConfig


    @ExceptionHandler(ApplianceNotFoundException::class)
    @ResponseBody
    fun handleApplianceNotFoundException(e: ApplianceNotFoundException) = ResponseEntity(
        ErrorResponse(e.message ?: "No such appliance with the specified sensor name"),
        HttpStatus.NOT_FOUND
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