@file:Suppress("WildcardImport")
package es.unizar.urlshortener.infrastructure.repositories

import es.unizar.urlshortener.core.*

/**
 * Implementation of the port [ClickRepositoryService].
 */
class ClickRepositoryServiceImpl(
    private val clickEntityRepository: ClickEntityRepository
) : ClickRepositoryService {
    override fun save(cl: Click): Click = clickEntityRepository.save(cl.toEntity()).toDomain()
}

/**
 * Implementation of the port [ShortUrlRepositoryService].
 */
class ShortUrlRepositoryServiceImpl(
    private val shortUrlEntityRepository: ShortUrlEntityRepository
) : ShortUrlRepositoryService {
    override fun findByKey(id: String): ShortUrl? = shortUrlEntityRepository.findByHash(id)?.toDomain()

    override fun save(su: ShortUrl): ShortUrl = shortUrlEntityRepository.save(su.toEntity()).toDomain()

    override fun update(hash: String, location: LocationData) {
        val shortUrlEntity = shortUrlEntityRepository.findByHash(hash)
        shortUrlEntity?.let {
            it.lat = location.lat
            it.lon = location.lon
            it.country = location.country
            it.city = location.city
            it.state = location.state
            it.road = location.road
            it.cp = location.cp
            shortUrlEntityRepository.save(it)
        }
    }

    override fun updateSafe(hash: String, flag: Boolean) {
        val shortUrlEntity = shortUrlEntityRepository.findByHash(hash)
        shortUrlEntity?.let {
            it.safe = flag
            shortUrlEntityRepository.save(it)
        }
    }
}
