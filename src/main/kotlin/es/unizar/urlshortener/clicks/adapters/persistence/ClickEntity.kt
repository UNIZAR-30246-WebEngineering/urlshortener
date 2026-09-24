package es.unizar.urlshortener.clicks.adapters.persistence

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "click")
class ClickEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var hash: String,
    var clientIp: String? = null,
    var occurredAt: Instant = Instant.now(),
)
