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

    override fun update(url: String, location: LocationData) {
        println("Before update:")
        val shortUrlEntity = shortUrlEntityRepository.findByHash(url)
        println("Hash: " + shortUrlEntity?.hash
                + " Lat: " + shortUrlEntity?.lat
                + " Lon: " + shortUrlEntity?.lon
                + " Country: " + shortUrlEntity?.country
                + " City: " + shortUrlEntity?.city
                + " State: " + shortUrlEntity?.state + " Road: "
                + shortUrlEntity?.road + " CP: "
                + shortUrlEntity?.cp)

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

        println("\nAfter update:")
        val shortUpdated = shortUrlEntityRepository.findByHash(url)
        println("Hash: " + shortUpdated?.hash
                + " Lat: " + shortUpdated?.lat
                + " Lon: " + shortUpdated?.lon
                + " Country: " + shortUpdated?.country
                + " City: " + shortUpdated?.city
                + " State: " + shortUpdated?.state
                + " Road: " + shortUpdated?.road
                + " CP: " + shortUpdated?.cp)
    }

    override fun updateSafe(hash: String, flag: Boolean) {
        val shortUrlEntity = shortUrlEntityRepository.findByHash(hash)
        shortUrlEntity?.let {
            it.safe = flag
            shortUrlEntityRepository.save(it)
        }
    }
}
