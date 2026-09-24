package es.unizar.urlshortener.analytics.application

import es.unizar.urlshortener.clicks.ClickLogged
import es.unizar.urlshortener.links.ShortUrlCreated
import org.jmolecules.architecture.hexagonal.PrimaryPort
import org.jmolecules.architecture.hexagonal.SecondaryPort

/** Per-hash click totals (one row per short URL). Not the permanent click log. */
data class LinkStats(
    val hash: String,
    val totalClicks: Long,
)

@SecondaryPort
interface LinkStatsStore {
    fun save(stats: LinkStats)

    fun findByHash(hash: String): LinkStats?
}

@PrimaryPort
interface UpdateLinkStats {
    fun onCreated(event: ShortUrlCreated)

    fun onClick(event: ClickLogged)
}

@PrimaryPort
interface GetLinkStats {
    fun findByHash(hash: String): LinkStats?
}
