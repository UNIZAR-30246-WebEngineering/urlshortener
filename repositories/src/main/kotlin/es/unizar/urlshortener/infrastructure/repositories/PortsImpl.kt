package es.unizar.urlshortener.infrastructure.repositories

import es.unizar.urlshortener.core.Click
import es.unizar.urlshortener.core.ClickRepositoryService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import es.unizar.urlshortener.core.IPLocationNotFound
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.TimeFrame
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Implementation of the port [ClickRepositoryService].
 */
class ClickRepositoryServiceImpl(
    private val clickEntityRepository: ClickEntityRepository
) : ClickRepositoryService {
    /**
     * Saves a [Click] entity to the repository.
     *
     * @param cl The [Click] entity to be saved.
     * @return The saved [Click] entity.
     */
    override fun save(cl: Click): Click {
        // Check if IP is not null and retrieve the country
        val country = cl.properties.ip?.let { getCountryByIp(it) }

        // Add country to ClickProperties
        val updatedClick = cl.copy(properties = cl.properties.copy(country = country))

        println("IP: ${cl.properties.ip}, Country: $country") // Log IP and Country

        return clickEntityRepository.save(updatedClick.toEntity()).toDomain()
    }

    /**
     * Recovers clicks based on timeframe and returns them as domain objects
     */
    override fun findClicksByTimeFrame(frame: TimeFrame): List<Click> {
        // Convert Long (millis) to OffsetDateTime
        val start = OffsetDateTime.ofInstant(Instant.ofEpochMilli(frame.start), ZoneOffset.UTC)
        val end = OffsetDateTime.ofInstant(Instant.ofEpochMilli(frame.end), ZoneOffset.UTC)

        // Call repository with converted times
        val clickEntities = clickEntityRepository.findClicksByTimeFrame(start, end)
        return clickEntities.map { it.toDomain() }
    }

    /**
     * Retrieves the country of origin for an IP address using the ip-api.com external service.
     *
     * @param ipAddress The IP address to get the country of origin from.
     * @return The country name or null if information couldn't be retrieved.
     */
    fun getCountryByIp(ipAddress: String): String? {
        val url = URL("http://ip-api.com/json/$ipAddress")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        @Suppress("SwallowedException")
        return try {
            connection.inputStream.bufferedReader().use { reader ->
                val response = reader.readText()
                val json = Json.parseToJsonElement(response).jsonObject
                if (json["status"]?.jsonPrimitive?.content == "success") {
                    json["country"]?.jsonPrimitive?.content
                } else {
                    null
                }
            }
        } catch (e: IPLocationNotFound) {
            null
        } finally {
            connection.disconnect()
        }
    }
}


/**
 * Implementation of the port [ShortUrlRepositoryService].
 */
class ShortUrlRepositoryServiceImpl(
    private val shortUrlEntityRepository: ShortUrlEntityRepository
) : ShortUrlRepositoryService {
    /**
     * Finds a [ShortUrl] entity by its key.
     *
     * @param id The key of the [ShortUrl] entity.
     * @return The found [ShortUrl] entity or null if not found.
     */
    override fun findByKey(id: String): ShortUrl? = shortUrlEntityRepository.findByHash(id)?.toDomain()

    /**
     * Saves a [ShortUrl] entity to the repository.
     *
     * @param su The [ShortUrl] entity to be saved.
     * @return The saved [ShortUrl] entity.
     */
    override fun save(su: ShortUrl): ShortUrl = shortUrlEntityRepository.save(su.toEntity()).toDomain()
}
