@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*

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
    private val reachableURIUseCase: ReachableURIUseCase
) : CreateShortUrlUseCase {
    // Create a short url from a long url
    override fun create(url: String, data: ShortUrlProperties): ShortUrl =
        // Check if the url is valid and reachable
        if (validatorService.isValid(url) && reachableURIUseCase.reachable(url)) {
            val id: String = hashService.hasUrl(url)
            val su = ShortUrl(
                hash = id,
                redirection = Redirection(target = url),
                properties = ShortUrlProperties(
                    safe = data.safe,
                    ip = data.ip,
                    sponsor = data.sponsor
                )
            )
            shortUrlRepository.save(su)
        }
        else {
            throw InvalidUrlException(url)
        }
}
