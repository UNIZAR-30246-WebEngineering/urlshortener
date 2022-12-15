package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.QRCodeUseCase
import es.unizar.urlshortener.core.usecases.RedirectUseCase
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.web.bind.annotation.*
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.exists

/**
 * The specification of the controller.
 */
interface UrlShortenerControllerRPC {

    /**
     * Redirects and logs a short url identified by its [id].
     *
     * **Note**: Delivery of use cases [RedirectUseCase] and [LogClickUseCase].
     */
    fun redirectTo(@Payload id: String): String

    /**
     * Creates a short url from details provided in [data].
     *
     * **Note**: Delivery of use case [CreateShortUrlUseCase].
     */
    fun shortener(@Payload data: String): String

    fun qr(@Payload id: String): String
}
/**
 * The implementation of the controller.
 *
 * **Note**: Spring Boot is able to discover this [RestController] without further configuration.
 */
@RestController
class UrlShortenerControllerRPCImpl (
    val redirectUseCase: RedirectUseCase,
    val logClickUseCase: LogClickUseCase,
    val createShortUrlUseCase: CreateShortUrlUseCase,
    val qrCodeUseCase: QRCodeUseCase
) : UrlShortenerControllerRPC {

    @MessageMapping("redirect")
    override fun redirectTo(@Payload id: String): String {
        redirectUseCase.redirectTo(id).let {
            return it.target
        }
    }

    @MessageMapping("create")
    override fun shortener(@Payload data: String): String {
        var data = data.split(" ")

        createShortUrlUseCase.create(
            url = data[0],
            data = ShortUrlProperties(
                limit = if (data[2] == "0") 0 else data[2].toInt(),
                qr = data[1] == "qr"
            )
        ).let {
            when (it.properties.safe) {
                true  -> return "http://localhost:8080/${it.hash}"
                false -> return "URI de destino no segura"
                else  -> return "URI de destino no validada todavía"
            }
        }
    }


    @MessageMapping("qr")
    override fun qr(@Payload id: String): String {
        val path = Paths.get("src/main/resources/static/qr/$id.png")
        if (path.exists()) {
            return "http://localhost:8080/$id/qr"
        } else {
            return "No existe ningún qr con ese hash: $id"
        }
    }
}