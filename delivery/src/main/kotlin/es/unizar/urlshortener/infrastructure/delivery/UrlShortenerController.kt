package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.QrCodeUseCase
import es.unizar.urlshortener.core.usecases.ReachableWebUseCase
import es.unizar.urlshortener.core.usecases.RedirectUseCase
import org.springframework.core.io.ByteArrayResource
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import javax.servlet.http.HttpServletRequest

/**
 * The specification of the controller.
 */
interface UrlShortenerController {

    /**
     * Redirects and logs a short url identified by its [id].
     *
     * **Note**: Delivery of use cases [RedirectUseCase] and [LogClickUseCase].
     */
    fun redirectTo(id: String, request: HttpServletRequest): ResponseEntity<Void>

    /**
     * Creates a short url from details provided in [data].
     *
     * **Note**: Delivery of use case [CreateShortUrlUseCase].
     */
    fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut>

    fun generateQrCode(id: String, request: HttpServletRequest): ResponseEntity<ByteArrayResource>
}

/**
 * Data required to create a short url.
 */
data class ShortUrlDataIn(
    val url: String,
    val sponsor: String? = null,
    val qr: Boolean
)

/**
 * Data returned after the creation of a short url.
 */
data class ShortUrlDataOut(
    val url: URI? = null,
    val properties: Map<String, Any> = emptyMap()
)

/**
 * The implementation of the controller.
 *
 * **Note**: Spring Boot is able to discover this [RestController] without further configuration.
 */
@RestController
class UrlShortenerControllerImpl(
    val redirectUseCase: RedirectUseCase,
    val logClickUseCase: LogClickUseCase,
    val createShortUrlUseCase: CreateShortUrlUseCase,
    val qrCodeUseCase: QrCodeUseCase,
    val reachableWebUseCase: ReachableWebUseCase
) : UrlShortenerController {

    @GetMapping("/{id:(?!api|index).*}")
    override fun redirectTo(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<Void> =

        redirectUseCase.redirectTo(id).let { redirect ->
            reachableWebUseCase.reachable(redirect.target).let {
                logClickUseCase.logClick(id, ClickProperties(ip = request.remoteAddr))
                val h = HttpHeaders()
                h.location = URI.create(redirect.target)
                ResponseEntity<Void>(h, HttpStatus.valueOf(redirect.mode))
            }
        }

    @PostMapping("/api/link", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    override fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut> =
        reachableWebUseCase.reachable(data.url).let {
            createShortUrlUseCase.create(
                url = data.url,
                data = ShortUrlProperties(
                    ip = request.remoteAddr,
                    sponsor = data.sponsor
                )
            ).let {
                val h = HttpHeaders()
                val url = linkTo<UrlShortenerControllerImpl> { redirectTo(it.hash, request) }.toUri()
                h.location = url

                val response = ShortUrlDataOut(
                    url = url,
                    properties = when (data.qr) {
                        false -> mapOf(
                            "safe" to it.properties.safe
                        )
                        true -> mapOf(
                            "safe" to it.properties.safe,
                            "qr" to linkTo<UrlShortenerControllerImpl> { generateQrCode(it.hash, request) }.toUri()
                        )
                    }
                )
                ResponseEntity<ShortUrlDataOut>(response, h, HttpStatus.CREATED)
            }
        }

    @GetMapping("/{id:(?!api|index).*}/qr")
    override fun generateQrCode(
        @PathVariable id: String,
        request: HttpServletRequest
    ): ResponseEntity<ByteArrayResource> =

        qrCodeUseCase.generateQR(
            id,
            linkTo<UrlShortenerControllerImpl> { redirectTo(id, request) }.toString()
        ).let {
            ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(ByteArrayResource(it, IMAGE_PNG_VALUE))
        }
}
