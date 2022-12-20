@file:Suppress("NoWildcardImports", "WildcardImport", "SpreadOperator")
package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*

/**
 * Given a hash returns the info related to the shortened URL.
 */
interface InfoUseCase {
    fun getInfo(hash: String): URLData
}

/**
 * Data returned after the creation of a short url.
 */
data class URLData(
    val url: String,
    val target: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val qr: String? = null,
    val safe: Boolean? = null,
)

/**
 * Implementation of [InfoUseCase].
 */
class InfoUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val redirectionLimitService: RedirectionLimitService
) : InfoUseCase {

    override fun getInfo(hash: String): URLData {
        shortUrlRepository.findByKey(hash)?.let {
            // This throws 429 only if redirection exists but rate limit has been reached
            // add special parameter here too
            redirectionLimitService.checkLimit(hash, false)

            // Been validated but it's not safe
            it.properties.safe?.let { safe ->
                if (!safe) {
                    throw RedirectUnsafeException() // Throw 403 exists but cant be used for redirections
                }
            }?: throw RedirectionNotValidatedException(RETRY_AFTER) // 400 if URI exists but not confirmed if it's safe

            return URLData(
                url = "http://localhost:8080/$hash",
                target = it.redirection.target,
                lat = it.properties.lat,
                lon = it.properties.lon,
                qr = if (it.properties.qr == true) "http://localhost:8080/$hash/qr" else null,
                safe = it.properties.safe
            )
        }?: run {
            throw RedirectionNotFound(hash)
        }
    }
}
