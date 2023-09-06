package edu.monash.smartcity.idledevicemanagement.controller

import app.urbanflo.urbanflosumoserver.model.ErrorResponse
import com.fasterxml.jackson.core.JsonProcessingException
import edu.monash.smartcity.idledevicemanagement.model.IpAddressData
import edu.monash.smartcity.idledevicemanagement.model.OccupancyData
import edu.monash.smartcity.idledevicemanagement.model.PlugStatusData
import edu.monash.smartcity.idledevicemanagement.model.PowerData
import edu.monash.smartcity.idledevicemanagement.service.ApplianceService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody

private val logger = KotlinLogging.logger {}

@Controller
class DataUpdateController(val applianceService: ApplianceService) {

    @PostMapping("/power")
    @ResponseBody
    fun updatePowerData(@RequestBody data: PowerData) {
        applianceService.updatePowerData(data)
    }

    @PostMapping("/occupancy")
    @ResponseBody
    fun updateOccupancyData(@RequestBody data: OccupancyData) {
        applianceService.updateOccupancyData(data)
    }

    @PostMapping("/plug-status")
    @ResponseBody
    fun updatePlugStatusData(@RequestBody data: PlugStatusData) {
        applianceService.updatePlugStatusData(data)
    }

    @PostMapping("/ip-address")
    @ResponseBody
    fun updateIpAddress(@RequestBody data: IpAddressData) {
        applianceService.updateIpAddress(data)
    }

    @ExceptionHandler(JsonProcessingException::class)
    @ResponseBody
    fun handleJsonError(e: JsonProcessingException): ResponseEntity<ErrorResponse> {
        logger.error(e) { "Invalid JSON received" }
        return ResponseEntity(ErrorResponse("Invalid JSON received."), HttpStatus.BAD_REQUEST)
    }
}