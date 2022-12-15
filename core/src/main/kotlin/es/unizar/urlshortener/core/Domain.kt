package es.unizar.urlshortener.core

import java.time.OffsetDateTime

/**
 * A [Click] captures a request of redirection of a [ShortUrl] identified by its [hash].
 */
data class Click(
    val hash: String,
    val properties: ClickProperties = ClickProperties(),
    val created: OffsetDateTime = OffsetDateTime.now()
)

/**
 * A [ShortUrl] is the mapping between a remote url identified by [redirection] and a local short url identified 
 * by [hash].
 */
data class ShortUrl(
    val hash: String,
    val redirection: Redirection,
    val created: OffsetDateTime = OffsetDateTime.now(),
    val properties: ShortUrlProperties = ShortUrlProperties()
)

/**
 * A [LocationData] specifies the information about a location
 */
data class LocationData(
    val lat: Double? = null,
    val lon: Double? = null,
    val country: String? = null,
    val city: String? = null,
    val state: String? = null,
    val road: String? = null,
    val cp: String? = null,
)

/**
 * A [Redirection] specifies the [target] and the [status code][mode] of a redirection.
 * By default, the [status code][mode] is 307 TEMPORARY REDIRECT.
 */
data class Redirection(
    val target: String,
    val mode: Int = 307
)

/**
 * A [ShortUrlProperties] is the bag of properties that a [ShortUrl] may have.
 */
data class ShortUrlProperties(
    val ip: String? = null,
    val sponsor: String? = null,
    val safe: Boolean? = null,
    val owner: String? = null,
    val lon: Double? = null,
    val lat: Double? = null,
    val country: String? = null,
    val city: String? = null,
    val state: String? = null,
    val road: String? = null,
    val cp: String? = null,
    val limit: Int = 0,
    val qr: Boolean? = false
)

/**
 * A [ClickProperties] is the bag of properties that a [Click] may have.
 */
data class ClickProperties(
    val ip: String? = null,
    val referrer: String? = null,
    val browser: String? = null,
    val platform: String? = null,
    val lon: Double? = null,
    val lat: Double? = null,
    val country: String? = null,
    val city: String? = null,
    val state: String? = null,
    val road: String? = null,
    val cp: String? = null,
)

data class ShortURLQRCode (
    val qrcode: ByteArray,
    val filename: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShortURLQRCode

        if (!qrcode.contentEquals(other.qrcode)) return false
        if (filename != other.filename) return false

        return true
    }

    override fun hashCode(): Int {
        var result = qrcode.contentHashCode()
        result = 31 * result + filename.hashCode()
        return result
    }
}
