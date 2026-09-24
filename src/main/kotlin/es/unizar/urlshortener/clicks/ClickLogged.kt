package es.unizar.urlshortener.clicks

import java.time.Instant

/**
 * Published on each successful redirect lookup. Owned by clicks; links publishes it.
 */
data class ClickLogged(
    val hash: String,
    val clientIp: String?,
    val occurredAt: Instant = Instant.now(),
)
