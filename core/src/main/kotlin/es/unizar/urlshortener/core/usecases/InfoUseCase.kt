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
) : InfoUseCase {

    override fun getInfo(hash: String): URLData {
        shortUrlRepository.findByKey(hash)?.let {

            if (it.properties.qr == true)
                println("pito" + it.properties.qr)
            else {
                println("pato" + it.properties.qr)
            }

            return URLData(
                url = it.redirection.target,
                target = "http://localhost:8080/$hash",
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