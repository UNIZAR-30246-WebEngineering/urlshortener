package es.unizar.urlshortener.analytics.application

import es.unizar.urlshortener.clicks.ClickLogged
import es.unizar.urlshortener.links.ShortUrlCreated
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
class LinkStatsService(
    private val store: LinkStatsStore,
) : UpdateLinkStats,
    GetLinkStats {
    @Transactional
    override fun onCreated(event: ShortUrlCreated) {
        store.save(LinkStats(hash = event.hash, totalClicks = 0))
        log.info { "stats ready for ${event.hash}" }
    }

    @Transactional
    override fun onClick(event: ClickLogged) {
        val current = store.findByHash(event.hash) ?: LinkStats(hash = event.hash, totalClicks = 0)
        store.save(current.copy(totalClicks = current.totalClicks + 1))
    }

    @Transactional(readOnly = true)
    override fun findByHash(hash: String): LinkStats? = store.findByHash(hash)
}
