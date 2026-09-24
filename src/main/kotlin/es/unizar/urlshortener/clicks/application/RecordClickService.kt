package es.unizar.urlshortener.clicks.application

import es.unizar.urlshortener.clicks.ClickLogged
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordClickService(
    private val store: ClickStore,
) : RecordClick {
    @Transactional
    override fun record(event: ClickLogged) {
        store.save(
            Click(hash = event.hash, clientIp = event.clientIp, occurredAt = event.occurredAt),
        )
    }
}
