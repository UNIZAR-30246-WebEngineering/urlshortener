package es.unizar.urlshortener.analytics.adapters.persistence

import es.unizar.urlshortener.analytics.application.LinkStats
import es.unizar.urlshortener.analytics.application.LinkStatsStore
import org.springframework.stereotype.Component

@Component
class JpaLinkStatsStore(
    private val repository: JpaLinkStatsRepository,
) : LinkStatsStore {
    override fun save(stats: LinkStats) {
        repository.save(
            LinkStatsEntity(hash = stats.hash, totalClicks = stats.totalClicks),
        )
    }

    override fun findByHash(hash: String): LinkStats? = repository.findById(hash).map { it.toDomain() }.orElse(null)

    private fun LinkStatsEntity.toDomain() = LinkStats(hash = hash, totalClicks = totalClicks)
}
