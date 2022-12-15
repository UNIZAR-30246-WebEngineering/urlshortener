package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.InvalidUrlException
import es.unizar.urlshortener.core.Redirection
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.UrlNotSafe
import es.unizar.urlshortener.core.ValidatorService
import java.util.concurrent.TimeUnit

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
) : CreateShortUrlUseCase {
    override fun create(url: String, data: ShortUrlProperties): ShortUrl {
        shortUrlRepository.latchUp()
        if (validatorService.isValid(url)) {
            val id: String = hashService.hasUrl(url)
            val su = ShortUrl(
                hash = id,
                redirection = Redirection(target = url),
                properties = ShortUrlProperties(
                    ip = data.ip,
                    sponsor = data.sponsor
                )
            )
            validatorService.sendToRabbit(url, id)

            shortUrlRepository.save(su)

            shortUrlRepository.getLatchFunction().await(10000, TimeUnit.MILLISECONDS)

            val shortUrlNew = shortUrlRepository.findByKey(id)

            if(!shortUrlNew?.properties?.safe!!){
                throw UrlNotSafe(url)
            } else {
                return su
            }

        } else {
            throw InvalidUrlException(url)
        }
    }
}
