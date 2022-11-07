package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.ValidatorService
import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.LocationService
import es.unizar.urlshortener.core.LocationData
import es.unizar.urlshortener.core.Redirection
import es.unizar.urlshortener.core.InvalidUrlException

/**
 * Given an url returns the key that is used to create a short URL.
 * When the url is created optional data may be added.
 *
 * **Note**: This is an example of functionality.
 */
interface CreateShortUrlUseCase {
    fun create(url: String, data: ShortUrlProperties): ShortUrl
}

/**
 * Implementation of [CreateShortUrlUseCase].
 */
class CreateShortUrlUseCaseImpl(
        private val shortUrlRepository: ShortUrlRepositoryService,
        private val validatorService: ValidatorService,
        private val hashService: HashService,
        private val locationService: LocationService
) : CreateShortUrlUseCase {
    override fun create(url: String, data: ShortUrlProperties): ShortUrl =
        if (validatorService.isValid(url) && validatorService.isReachable(url)) {
            // Get the location from the coordinates or the ip
            val location: LocationData = locationService.getLocation(data.lat, data.lon, data.ip)

            val id: String = hashService.hasUrl(url)
            val su = ShortUrl(
                hash = id,
                redirection = Redirection(target = url),
                properties = ShortUrlProperties(
                    safe = data.safe,
                    ip = data.ip,
                    sponsor = data.sponsor,
                    lat = location.lat,
                    lon = location.lon,
                    country = location.country,
                    city = location.city,
                    state = location.state,
                    road = location.road,
                    cp = location.cp,
                )
            )
            shortUrlRepository.save(su)
        } else {
            throw InvalidUrlException(url)
        }
}
