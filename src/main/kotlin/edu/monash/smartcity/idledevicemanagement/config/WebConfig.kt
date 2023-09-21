package edu.monash.smartcity.idledevicemanagement.config

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {
    @Value("\${idle-device-management.frontend-url}")
    private lateinit var frontendUrl: String

    @Value("\${idle-device-management.allow-all-cors-origins}")
    private var allowAllCorsOrigins: Boolean = false

    override fun addCorsMappings(registry: CorsRegistry) {
        if (allowAllCorsOrigins) {
            registry.addMapping("/**").allowedOriginPatterns("*")
        } else {
            registry.addMapping("/**").allowedOrigins(frontendUrl)
        }
    }

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        // Always write date/time in ISO format instead of unix timestamp
        val objectMapper = jacksonObjectMapper()
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.registerModules(JavaTimeModule())
        // https://github.com/springdoc/springdoc-openapi/issues/2143
        converters.add(ByteArrayHttpMessageConverter())
        converters.add(MappingJackson2HttpMessageConverter(objectMapper))
    }
}