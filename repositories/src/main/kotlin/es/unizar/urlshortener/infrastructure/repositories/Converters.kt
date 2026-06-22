@file:Suppress("WildcardImport")

package es.unizar.urlshortener.infrastructure.repositories

import es.unizar.urlshortener.core.*

/**
 * Extension method to convert a [ClickEntity] into a domain [Click].
 */
fun ClickEntity.toDomain() = Click(
    hash = UrlHash(hash),
    created = created,
    properties = ClickProperties(
        ip = ip?.let { IpAddress(it) },
        referrer = referrer?.let { Referrer(it) },
        browser = browser?.let { Browser(it) },
        platform = platform?.let { Platform(it) },
        country = country?.let { CountryCode(it) }
    )
)

/**
 * Extension method to convert a domain [Click] into a [ClickEntity].
 */
fun Click.toEntity() = ClickEntity(
    id = null,
    hash = hash.value,
    created = created,
    ip = properties.ip?.value,
    referrer = properties.referrer?.value,
    browser = properties.browser?.value,
    platform = properties.platform?.value,
    country = properties.country?.value
)

/**
 * Extension method to convert a [ShortUrlEntity] into a domain [ShortUrl].
 */
fun ShortUrlEntity.toDomain() = ShortUrl(
    hash = UrlHash(hash),
    redirection = Redirection(
        target = Url(target),
        type = when (mode) {
            HttpStatusCodes.PERMANENT_REDIRECT -> RedirectionType.Permanent
            else -> RedirectionType.Temporary
        }
    ),
    created = created,
    properties = ShortUrlProperties(
        sponsor = sponsor?.let { Sponsor(it) },
        owner = owner?.let { Owner(it) },
        safety = when (safe) {
            true -> UrlSafety.Safe
            false -> UrlSafety.Unsafe
        },
        ip = ip?.let { IpAddress(it) },
        country = country?.let { CountryCode(it) }
    )
)

/**
 * Extension method to convert a domain [ShortUrl] into a [ShortUrlEntity].
 */
fun ShortUrl.toEntity() = ShortUrlEntity(
    hash = hash.value,
    target = redirection.target.value,
    mode = redirection.statusCode,
    created = created,
    owner = properties.owner?.value,
    sponsor = properties.sponsor?.value,
    safe = properties.safety == UrlSafety.Safe,
    ip = properties.ip?.value,
    country = properties.country?.value
)
