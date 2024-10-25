package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.RedirectUseCase
import jakarta.servlet.http.HttpServletRequest
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.net.MalformedURLException
import java.io.IOException
import org.slf4j.LoggerFactory

/**
 * The specification of the controller.
 */
interface UrlShortenerController {

    /**
     * Redirects and logs a short url identified by its [id].
     *
     * **Note**: Delivery of use cases [RedirectUseCase] and [LogClickUseCase].
     */
    fun redirectTo(id: String, request: HttpServletRequest): ResponseEntity<Unit>

    /**
     * Creates a short url from details provided in [data].
     *
     * **Note**: Delivery of use case [CreateShortUrlUseCase].
     */
    fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut>
}

/**
 * Data required to create a short url.
 */
data class ShortUrlDataIn(
    val url: String,
    val sponsor: String? = null
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
    val createShortUrlUseCase: CreateShortUrlUseCase
) : UrlShortenerController {
    private val logger = LoggerFactory.getLogger(UrlShortenerControllerImpl::class.java)

    fun determinePlatform(userAgent: String?): String {
        return when {
            userAgent == null -> "Unknown"
            userAgent.contains("Windows", true) -> "Windows"
            userAgent.contains("Mac", true) -> "MacOS"
            userAgent.contains("Linux", true) -> "Linux"
            else -> "Other"
        }
    }

    fun simplifyBrowserName(userAgent: String): String {
        return when {
            userAgent.contains("Chrome", ignoreCase = true) -> "Chrome"
            userAgent.contains("Firefox", ignoreCase = true) -> "Firefox"
            userAgent.contains("Safari", ignoreCase = true) &&
                !userAgent.contains("Chrome", ignoreCase = true) -> "Safari"
            userAgent.contains("Edge", ignoreCase = true) -> "Edge"
            userAgent.contains("Opera", ignoreCase = true) ||
                    userAgent.contains("OPR", ignoreCase = true) -> "Opera"
            userAgent.contains("Trident", ignoreCase = true) -> "Internet Explorer"
            else -> "Unknown"
        }
    }

    fun getCountryFromIp(ip: String): String? {
        //Uses https://ip-api.com/ as an external service to get the location
        // if you are on private ip, try out 24.48.0.1 in request to check functionality
        val url = "http://ip-api.com/json/$ip"
        var connection: HttpURLConnection? = null

        return try {
            // Create HTTP conn
            val urlObj = URL(url)
            connection = urlObj.openConnection() as HttpURLConnection
            connection.requestMethod = "GET" // GET request type

            // Verify reques code first
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                // Read repsonse
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                // Parse JSON to get "Country"... change for more things if required
                val json = JSONObject(response)
                json.optString("country")
            } else {
                // Maneja el error según el código de respuesta
                println("Error: ${connection.responseCode} ${connection.responseMessage}")
                null
            }
        } catch (e: MalformedURLException) {
            logger.error("MalformedURL Exception: ${e.localizedMessage}")
            null
        } catch (e: IOException) {
            logger.error("IO Exception: ${e.localizedMessage}")
            null
        } finally {
            connection?.disconnect() // Asegúrate de desconectar la conexión
        }
    }

    /**
     * Redirects and logs a short url identified by its [id].
     *
     * @param id the identifier of the short url
     * @param request the HTTP request
     * @return a ResponseEntity with the redirection details
     */
    @GetMapping("/{id:(?!api|index).*}")
    override fun redirectTo(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<Unit> =
        redirectUseCase.redirectTo(id).run {
            logClickUseCase.logClick(id, ClickProperties(
                ip = request.remoteAddr,
                referrer = request.getHeader("Referer"),
                browser = simplifyBrowserName(request.getHeader("User-Agent")),
                platform = determinePlatform(request.getHeader("User-Agent")),
                country = getCountryFromIp(request.remoteAddr)
            ))
            val h = HttpHeaders()
            h.location = URI.create(target)
            ResponseEntity<Unit>(h, HttpStatus.valueOf(mode))
        }

    /**
     * Creates a short url from details provided in [data].
     *
     * @param data the data required to create a short url
     * @param request the HTTP request
     * @return a ResponseEntity with the created short url details
     */
    @PostMapping("/api/link", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    override fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut> =
        createShortUrlUseCase.create(
            url = data.url,
            data = ShortUrlProperties(
                ip = request.remoteAddr,
                sponsor = data.sponsor
            )
        ).run {
            val h = HttpHeaders()
            val url = linkTo<UrlShortenerControllerImpl> { redirectTo(hash, request) }.toUri()
            h.location = url
            val response = ShortUrlDataOut(
                url = url,
                properties = mapOf(
                    "safe" to properties.safe
                )
            )
            ResponseEntity<ShortUrlDataOut>(response, h, HttpStatus.CREATED)
        }
}
