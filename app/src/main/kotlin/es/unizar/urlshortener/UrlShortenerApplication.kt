package es.unizar.urlshortener

import es.unizar.urlshortener.core.RabbitMQService
import es.unizar.urlshortener.infrastructure.delivery.RabbitMQServiceImpl
import es.unizar.urlshortener.infrastructure.delivery.ValidatorServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.ShortUrlRepositoryServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.ShortUrlEntityRepository
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    val rabbit = RabbitMQServiceImpl(shortUrlRepository = ShortUrlRepositoryServiceImpl(
        shortUrlEntityRepository = new ShortUrlEntityRepository()
    ), validator = ValidatorServiceImpl())
    GlobalScope.launch {
        while (true) {
            println("thread: ..........")
            rabbit.read()
            Thread.sleep(5000)
        }
    }
    runApplication<UrlShortenerApplication>(*args)
}
