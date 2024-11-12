package es.unizar.urlshortener.core

import java.time.Instant
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
 * Represents a time frame for retrieving click data.
 */
data class TimeFrame(
    val start: Long,
    val end: Long
) {
    init {
        require(end >= start) { "Start time must be before or equal to end time" }
    }
}

/**
 * Represents filters for click analytics.
 */
data class ClickFilters(
    val browser: String? = null,
    val referrer: String? = null,
    val country: String? = null,
    val platform: String? = null
)

/**
 * Represents click analytics data.
 */
data class ClickAnalytics(
    val timestamp: Long,
    val browser: String,
    val referrer: String,
    val country: String,
    val platform: String
)

/**
 * A [ShortUrl] is the mapping between a remote url identified by [redirection]
 * and a local short url identified by [hash].
 */
data class ShortUrl(
    val hash: String,
    val redirection: Redirection,
    val created: OffsetDateTime = OffsetDateTime.now(),
    val properties: ShortUrlProperties = ShortUrlProperties()
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
    val safe: Boolean = true,
    val owner: String? = null,
    val country: String? = null
)

/**
 * A [ClickProperties] is the bag of properties that a [Click] may have.
 */
data class ClickProperties(
    val ip: String? = null,
    val referrer: String? = null,
    val browser: String? = null,
    val platform: String? = null,
    val country: String? = null
)
