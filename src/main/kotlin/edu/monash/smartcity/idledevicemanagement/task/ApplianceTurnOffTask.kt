package edu.monash.smartcity.idledevicemanagement.task

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.InetAddress

private val logger = KotlinLogging.logger {}

class ApplianceTurnOffTask(private val ipAddress: InetAddress) : Runnable {
    private val restTemplate = RestTemplate()
    private val headers = initHttpHeaders()
    override fun run() {
        val url = UriComponentsBuilder.newInstance().scheme("http").host(ipAddress.hostAddress)
            .path("/switch/athom_smart_plug_v2/turn_off").build().toUri()
        val requset = HttpEntity<String>(headers)
        try {
            val response = restTemplate.postForEntity(url, requset, String::class.java)
            logger.info { "Response from $ipAddress: ${response.body} (status code ${response.statusCode}" }
        } catch (e: RestClientException) {
            logger.error(e) { "Cannot send turn off command to $ipAddress" }
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