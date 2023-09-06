package edu.monash.smartcity.idledevicemanagement.task

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.net.InetAddress

private val logger = KotlinLogging.logger {}

class ApplianceTurnOnTask(private val ipAddress: InetAddress) : Runnable {
    private val restTemplate = RestTemplate()
    private val headers = initHttpHeaders()
    override fun run() {
        val url = "http://$ipAddress/switch/athom_smart_plug_v2/turn_on"
        val requset = HttpEntity<String>(headers)
        try {
            restTemplate.postForEntity(url, requset, String::class.java)
        } catch (e: RestClientException) {
            logger.error(e) { "Cannot send turn on command to $ipAddress" }
        }
    }

    companion object {
        /**
         * Initializer for the HTTP header.
         *
         * @return an instance of [HttpHeaders] with `contentType` set to `application/json`
         */
        private fun initHttpHeaders(): HttpHeaders {
            val httpHeaders = HttpHeaders()
            httpHeaders.contentType = MediaType.APPLICATION_JSON
            return httpHeaders
        }
    }

}