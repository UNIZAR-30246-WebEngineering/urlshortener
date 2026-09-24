package es.unizar.urlshortener.links.adapters.persistence

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "short_url")
class ShortUrlEntity(
    @Id
    var hash: String,
    var target: String,
    var createdAt: Instant = Instant.now(),
    var creatorIp: String? = null,
)
