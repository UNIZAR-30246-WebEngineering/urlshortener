package es.unizar.urlshortener.core

/**
 * [ClickRepositoryService] is the port to the repository that provides persistence to [Clicks][Click].
 */
interface ClickRepositoryService {
    fun save(cl: Click): Click
}

/**
 * [ShortUrlRepositoryService] is the port to the repository that provides management to [ShortUrl][ShortUrl].
 */
interface ShortUrlRepositoryService {
    fun findByKey(id: String): ShortUrl?
    fun save(su: ShortUrl): ShortUrl
    fun update(url: String, location: LocationData)
}

/**
 * [QRService] is the port to the repository that generates QR Codes
 */
interface QRService {
    fun generateQRCode(uri: String, filename: String): ShortURLQRCode
}

/**
 * [ValidatorService] is the port to the service that validates if an url can be shortened.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core .
 */
interface ValidatorService {
    fun isValid(url: String): Boolean

    fun isReachable(url: String): Boolean

    fun isSecure(url: String): Boolean
}

/**
 * [LocationService] is the port to the service that provides location information.
 */
interface LocationService {
    suspend fun getLocation(lat: Double?, lon: Double?, ip: String?): LocationData
}

/**
 * [HashService] is the port to the service that creates a hash from a URL.
 *
 * **Note**: It is a design decision to create this port. It could be part of the core .
 */
interface HashService {
    fun hasUrl(url: String): String
}

/**
 * [RedirectionLimitService] is the port to the service that limits consecutive redirects
 */
interface RedirectionLimitService {

    fun addLimit(hash : String, limit : Int)
    fun checkLimit(hash : String)
}
