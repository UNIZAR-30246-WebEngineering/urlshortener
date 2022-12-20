@file:Suppress("WildcardImport")

package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.UnsafeURIException
import es.unizar.urlshortener.core.usecases.*
import org.springframework.core.io.ByteArrayResource
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.MediaType.IMAGE_PNG
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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

    fun information(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<URLData>

    /**
     * Creates a short url from details provided in [data].
     *
     * **Note**: Delivery of use case [CreateShortUrlUseCase].
     */
    fun shortenerJson(@RequestBody data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut>

    fun shortenerUnderCoded(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut>

    fun shortFun(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut>

    fun qr(id: String, request: HttpServletRequest): ResponseEntity<ByteArrayResource>
}
/**
 * Data required to create a short url.
 */
data class ShortUrlDataIn(
    val url: String,
    val sponsor: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val limit: Int? = null,
    val qr: Boolean? = null,
)

/**
 * Data returned after the creation of a short url.
 */
data class ShortUrlDataOut(
    val url: URI? = null,
    val target: String? = null,
    val limit: Int? = null,
    val lat : Double? = null,
    val lon : Double? = null,
    val qr: String? = null,
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
    val infoUseCase: InfoUseCase,
    val qrCodeUseCase: QRCodeUseCase
) : UrlShortenerController {

    @GetMapping("/{id:(?!api|index|docs|openapi).*}")
    override fun redirectTo(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<Void> {
        redirectUseCase.redirectTo(id, request.getHeader("Special-User") != null ).let {
            logClickUseCase.logClick(id, ClickProperties(ip = request.remoteAddr))
            val h = HttpHeaders()
            h.location = URI.create(it.target)
            return ResponseEntity<Void>(h, HttpStatus.valueOf(it.mode))
        }
    }

    @GetMapping("/api/link/{id}")
    override fun information(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<URLData> {
        val h = HttpHeaders()
        val url = linkTo<UrlShortenerControllerImpl> { redirectTo(id, request) }.toUri()
        h.location = url

        val response = infoUseCase.getInfo(id)
        return ResponseEntity<URLData>(response, h, HttpStatus.OK)
    }

    @PostMapping("/api/link", consumes = [MediaType.APPLICATION_JSON_VALUE])
    override fun shortenerJson(@RequestBody data: ShortUrlDataIn, request: HttpServletRequest):
            ResponseEntity<ShortUrlDataOut> =
        shortFun(data, request)

    @PostMapping("/api/link", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    override fun shortenerUnderCoded(data: ShortUrlDataIn, request: HttpServletRequest):
            ResponseEntity<ShortUrlDataOut> =
        shortFun(data, request)

    override fun shortFun(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut> =
        createShortUrlUseCase.create(
            url = data.url,
            data = ShortUrlProperties(
                ip = request.remoteAddr,
                sponsor = data.sponsor,
                lat = data.lat,
                lon = data.lon,
                limit = data.limit ?: 0,
                qr = data.qr
            ),

            ).let {shortUrl ->
                val h = HttpHeaders()
                val url = linkTo<UrlShortenerControllerImpl> { redirectTo(shortUrl.hash, request) }.toUri()
                h.location = url

            shortUrl.properties.safe?.let {
                    // Not null nula y safe (it) es true si no se lanza una excepción en create
                    val response = ShortUrlDataOut(
                        url = url,
                        target = data.url,
                        limit = data.limit,
                        lat = shortUrl.properties.lat,
                        lon = shortUrl.properties.lon,
                        qr = "http://localhost/${shortUrl.hash}/qr",
                    )
                    return ResponseEntity<ShortUrlDataOut>(response, h, HttpStatus.CREATED)

            // Es null no ha sido validada todavía
            } ?: run {
                val response = ShortUrlDataOut(
                    url = url,
                    target = data.url,
                    limit = data.limit,
                    lat = shortUrl.properties.lat,
                    lon = shortUrl.properties.lon,
                    qr = "http://localhost/${shortUrl.hash}/qr",
                    properties = mapOf("error" to "URI de destino no validada todavía")
                )
                return ResponseEntity<ShortUrlDataOut>(response, h, HttpStatus.CREATED)
            }
        }

    @GetMapping("{id:.*}/qr")
    override fun qr(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<ByteArrayResource> =
        qrCodeUseCase.getQR(id).let {
            val h = HttpHeaders()
            h.contentType = IMAGE_PNG
            return ResponseEntity.ok().contentType(IMAGE_PNG).body(ByteArrayResource(it.qrcode, IMAGE_PNG_VALUE))
        }
}
