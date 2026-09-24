package es.unizar.urlshortener.links.application

import es.unizar.urlshortener.clicks.ClickLoggedEvent
import es.unizar.urlshortener.links.ShortUrlCreatedEvent
import es.unizar.urlshortener.links.domain.ShortUrl
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class LinkService(
    private val store: ShortUrlStore,
    private val validator: UrlValidator,
    private val hashGenerator: HashGenerator,
    private val events: ApplicationEventPublisher,
) : CreateShortUrl,
    RedirectShortUrl {
    @Transactional
    override fun create(url: String): CreatedShortUrl {
        val trimmed = url.trim()
        require(trimmed.isNotEmpty()) { "url must not be blank" }
        if (!validator.isValid(trimmed)) {
            throw InvalidUrlException(trimmed)
        }
        return candidateHashes(trimmed)
            .firstNotNullOfOrNull { hash -> claim(hash, trimmed) }
            ?: throw HashCollisionException(trimmed, MAX_HASH_ATTEMPTS)
    }

    private fun candidateHashes(url: String): Sequence<String> =
        (0 until MAX_HASH_ATTEMPTS).asSequence().map { attempt ->
            hashGenerator.hash(if (attempt == 0) url else "$url#$attempt")
        }

    private fun claim(
        hash: String,
        url: String,
    ): CreatedShortUrl? {
        val existing = store.findByHash(hash).orElse(null)
        val stored = existing ?: store.saveIfAbsent(ShortUrl(hash = hash, target = url))
        if (stored.target != url) return null
        if (existing == null) events.publishEvent(ShortUrlCreatedEvent(hash = hash, target = url))
        return CreatedShortUrl(hash = hash, target = url)
    }

    @Transactional
    override fun redirect(hash: String): Redirection {
        val shortUrl = store.findByHash(hash).orElseThrow { LinkNotFoundException(hash) }
        events.publishEvent(ClickLoggedEvent(hash = hash, occurredAt = Instant.now()))
        return Redirection(target = shortUrl.target)
    }

    companion object {
        const val MAX_HASH_ATTEMPTS = 3
    }
}
