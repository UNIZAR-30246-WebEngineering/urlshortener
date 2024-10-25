package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.usecases.GeolocationUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/geolocation")
class GeolocationController(
    private val geolocationUseCase: GeolocationUseCase
) {

    @GetMapping("/{ip}")
    fun getGeolocation(@PathVariable ip: String): ResponseEntity<GeolocationResponse> {
        val geolocation = geolocationUseCase.getGeolocation(ip)
        return if (geolocation != null) {
            ResponseEntity(
                GeolocationResponse(
                    country = geolocation.country,
                    city = geolocation.city,
                    region = geolocation.region
                ), HttpStatus.OK
            )
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
}

data class GeolocationResponse(
    val country: String,
    val city: String,
    val region: String
)
