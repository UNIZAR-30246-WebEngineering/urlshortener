package es.unizar.urlshortener

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * The marker that makes this project a Spring Boot application.
 */
@OpenAPIDefinition
@SpringBootApplication
class UrlShortenerApplication

/**
 * The main entry point.
 */
fun main(vararg args: String) {
    System.setProperty("spring.profiles.active", "rpcserver")
    runApplication<UrlShortenerApplication>(*args)
}
