package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.Redirection
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.UrlNotSafe

/**
 * Given a key returns a [Redirection] that contains a [URI target][Redirection.target]
 * and an [HTTP redirection mode][Redirection.mode].
 *
 * **Note**: This is an example of functionality.
 */
interface RedirectUseCase {
    fun redirectTo(key: String): Redirection
}

/**
 * Implementation of [RedirectUseCase].
 */
class RedirectUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService
) : RedirectUseCase {
    override fun redirectTo(key: String): Redirection {
        val redirect = shortUrlRepository
            .findByKey(key)
            ?.redirection
            ?: throw RedirectionNotFound(key)

        val su = shortUrlRepository.findByKey(key)

        if (su != null) {
            if (!su.properties.safe) {
                throw UrlNotSafe(su.redirection.target)
            }
        }

        return redirect
    }
}
