@file:Suppress("NoWildcardImports", "WildcardImport", "SpreadOperator")
package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import kotlinx.coroutines.*

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
    private val locationService: LocationService,
) : CreateShortUrlUseCase {
    override fun create(url: String, data: ShortUrlProperties): ShortUrl {
        if (validatorService.isValid(url) && validatorService.isReachable(url) && validatorService.isSecure(url)) {
            val id: String = hashService.hasUrl(url)
            val su = ShortUrl(
                    hash = id,
                    redirection = Redirection(target = url),
                    properties = ShortUrlProperties(
                            safe = data.safe,
                            ip = data.ip,
                            sponsor = data.sponsor,
                    )
            )
            val shortUrl = shortUrlRepository.save(su)

            // Start the coroutine to get the location
            GlobalScope.launch {
                // Get the location from the coordinates or the ip
                val location: LocationData = locationService.getLocation(data.lat, data.lon, data.ip)
                // Update the shortUrl with the location
                shortUrlRepository.update(id, location)
            }
            println("Valor devuelto: " + shortUrl.hash + " " + shortUrl.redirection)
            return shortUrl
        } else {
            throw InvalidUrlException(url)
        }
    }
}
