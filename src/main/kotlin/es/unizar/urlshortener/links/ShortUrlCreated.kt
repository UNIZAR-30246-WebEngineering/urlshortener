package es.unizar.urlshortener.links

import java.time.Instant

/**
 * Published after a short URL is persisted.
 */
data class ShortUrlCreated(
    val hash: String,
    val target: String,
    val createdAt: Instant = Instant.now(),
)
