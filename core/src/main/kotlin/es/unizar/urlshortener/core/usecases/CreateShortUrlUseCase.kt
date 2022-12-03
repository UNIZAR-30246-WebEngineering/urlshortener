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
        private val redirectionLimitService: RedirectionLimitService,
        private val qrService: QRService,
        private val rabbit: RabbitMQService
) : CreateShortUrlUseCase {
    @DelicateCoroutinesApi
    override fun create(url: String, data: ShortUrlProperties): ShortUrl {
        if (validatorService.isValid(url) && validatorService.isReachable(url)) {
            val id: String = hashService.hasUrl(url)
            val su = ShortUrl(
                    hash = id,
                    redirection = Redirection(target = url),
                    properties = ShortUrlProperties(
                            ip = data.ip,
                            sponsor = data.sponsor,
                    )
            )
            val shortUrl = shortUrlRepository.save(su)

            rabbit.write(shortUrl.hash + " " + url)

            if (data.limit > 0) {
                redirectionLimitService.addLimit(id, data.limit)
            }

            // Start the coroutine to get the location
            GlobalScope.launch {
                val location = locationService.getLocation(data.lat, data.lon, data.ip)
                location.thenApply {
                    // Update the shortUrl with the location when completed
                    shortUrlRepository.update(id, it)
                }
            }

            // Start the coroutine to generate the qr
            GlobalScope.launch {
                val qrCode = qrService.generateQRCode(url, "$id.png")
                qrCode.thenApply {
                    // Save file when the qr is generated
                    qrService.saveQR(it)
                }
            }
            return shortUrl

        } else {
            throw InvalidUrlException(url)
        }
    }
}
