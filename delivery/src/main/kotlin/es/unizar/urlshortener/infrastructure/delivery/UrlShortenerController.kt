package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.RedirectUseCase
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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

}

/**
 * Data required to create a short url.
 */
data class ShortUrlDataIn(
    val url: String,
    val sponsor: String? = null,
    val lat: Double? = null,
    val lon: Double? = null
)

/**
 * Data returned after the creation of a short url.
 */
data class ShortUrlDataOut(
    val url: URI? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val country: String? = null,
    val city: String? = null,
    val state: String? = null,
    val road: String? = null,
    val cp: String? = null,
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

    @GetMapping("/tiny-{id:.*}")
    override fun redirectTo(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<Void> =
            redirectUseCase.redirectTo(id).let {
                logClickUseCase.logClick(id, ClickProperties(ip = request.remoteAddr))
                val h = HttpHeaders()
                h.location = URI.create(it.target)
                ResponseEntity<Void>(h, HttpStatus.valueOf(it.mode))
            }

    @PostMapping("/api/link", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    override fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut> =
            createShortUrlUseCase.create(
                    url = data.url,
                    data = ShortUrlProperties(
                            ip = request.remoteAddr,
                            sponsor = data.sponsor,
                            lat = data.lat,
                            lon = data.lon
                    )
            ).let {
                val h = HttpHeaders()
                val url = linkTo<UrlShortenerControllerImpl> { redirectTo(it.hash, request) }.toUri()
                h.location = url
                val response = ShortUrlDataOut(
                        url = url,
                        lat = it.properties.lat,
                        lon = it.properties.lon,
                        country = it.properties.country,
                        city = it.properties.city,
                        state = it.properties.state,
                        road = it.properties.road,
                        cp = it.properties.cp,
                        properties = mapOf(
                                "safe" to it.properties.safe
                        )
                )
                ResponseEntity<ShortUrlDataOut>(response, h, HttpStatus.CREATED)
            }
}

    /**
     * Get location from lat and lon.
     * Example of response from openstreetmap api:
     * https://nominatim.openstreetmap.org/reverse?format=json&lat=41.641412477417894&lon=-0.8800855922769534
     */
/*
    @PostMapping("/api/location", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun doGetLocation(@RequestBody data: LocationDataIn, request: HttpServletRequest): ResponseEntity<LocationDataOut> {
        var response = LocationDataOut()
        if (!data.lat.equals(360) && !data.lon.equals(360)) {
            val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=${data.lat}&lon=${data.lon}")
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"

            if (con.responseCode == 200) {
                val mapper = ObjectMapper()
                // Parsear el json devuelto
                val json = mapper.readTree(con.inputStream)

                response = LocationDataOut(
                        city = json.get("address").get("city").asText(),
                        country = json.get("address").get("country").asText()
                )
                con.disconnect()
                return ResponseEntity<LocationDataOut>(response, HttpStatus.OK)

            }
            con.disconnect()
        }
        // Obtenerla via ip
        val url = URL("https://iplist.cc/api/" + request.remoteAddr)
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = "GET"

        if (con.responseCode == 200) {
            val mapper = ObjectMapper()
            val json = mapper.readTree(con.inputStream)

            if (json.get("ip").asText() != "127.0.0.1") {
                response = LocationDataOut(
                        city = "",
                        country = json.get("countryname").asText()
                )
            }
            con.disconnect()
            return ResponseEntity<LocationDataOut>(response, HttpStatus.OK)
        }
        con.disconnect()
        return ResponseEntity<LocationDataOut>(HttpStatus.SERVICE_UNAVAILABLE)
    }
}*/


