package es.unizar.urlshortener.links.application

import es.unizar.urlshortener.clicks.ClickLogged
import es.unizar.urlshortener.links.ShortUrlCreated
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
    override fun create(
        url: String,
        creatorIp: String?,
    ): CreatedShortUrl {
        val trimmed = url.trim()
        require(trimmed.isNotEmpty()) { "url must not be blank" }
        if (!validator.isValid(trimmed)) {
            throw InvalidUrlException(trimmed)
        }
        val hash = hashGenerator.hash(trimmed)
        store.save(ShortUrl(hash = hash, target = trimmed, creatorIp = creatorIp))
        events.publishEvent(ShortUrlCreated(hash = hash, target = trimmed))
        return CreatedShortUrl(hash = hash, target = trimmed)
    }

    /**
     * Stateless redirect: lookup + publish [ClickLogged].
     */
    @Transactional
    override fun redirect(
        hash: String,
        clientIp: String?,
    ): Redirection {
        val shortUrl = store.findByHash(hash).orElseThrow { LinkNotFoundException(hash) }
        events.publishEvent(
            ClickLogged(hash = hash, clientIp = clientIp, occurredAt = Instant.now()),
        )
        return Redirection(target = shortUrl.target)
    }
}
