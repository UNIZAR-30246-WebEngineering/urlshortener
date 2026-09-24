package es.unizar.urlshortener.clicks.adapters.persistence

import es.unizar.urlshortener.clicks.application.Click
import es.unizar.urlshortener.clicks.application.ClickStore
import org.springframework.stereotype.Component

@Component
class JpaClickStore(
    private val repository: JpaClickRepository,
) : ClickStore {
    override fun save(click: Click) {
        repository.save(
            ClickEntity(
                hash = click.hash,
                clientIp = click.clientIp,
                occurredAt = click.occurredAt,
            ),
        )
    }
}
