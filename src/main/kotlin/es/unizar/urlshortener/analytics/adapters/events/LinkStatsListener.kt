package es.unizar.urlshortener.analytics.adapters.events

import es.unizar.urlshortener.analytics.application.UpdateLinkStats
import es.unizar.urlshortener.clicks.ClickLogged
import es.unizar.urlshortener.links.ShortUrlCreated
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class LinkStatsListener(
    private val updateLinkStats: UpdateLinkStats,
) {
    @ApplicationModuleListener
    fun onCreated(event: ShortUrlCreated) {
        updateLinkStats.onCreated(event)
    }

    @ApplicationModuleListener
    fun onClick(event: ClickLogged) {
        updateLinkStats.onClick(event)
    }
}
