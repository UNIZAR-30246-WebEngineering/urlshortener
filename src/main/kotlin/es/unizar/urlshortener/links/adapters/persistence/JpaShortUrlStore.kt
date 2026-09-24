package es.unizar.urlshortener.links.adapters.persistence

import es.unizar.urlshortener.links.application.ShortUrlStore
import es.unizar.urlshortener.links.domain.ShortUrl
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class JpaShortUrlStore(
    private val repository: JpaShortUrlRepository,
) : ShortUrlStore {
    override fun save(shortUrl: ShortUrl): ShortUrl {
        val saved = repository.save(shortUrl.toEntity())
        return saved.toDomain()
    }

    override fun findByHash(hash: String): Optional<ShortUrl> = repository.findById(hash).map { it.toDomain() }

    private fun ShortUrl.toEntity() =
        ShortUrlEntity(
            hash = hash,
            target = target,
            createdAt = createdAt,
            creatorIp = creatorIp,
        )

    private fun ShortUrlEntity.toDomain() =
        ShortUrl(
            hash = hash,
            target = target,
            createdAt = createdAt,
            creatorIp = creatorIp,
        )
}
