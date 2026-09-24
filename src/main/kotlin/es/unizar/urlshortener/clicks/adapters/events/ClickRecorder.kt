package es.unizar.urlshortener.clicks.adapters.events

import es.unizar.urlshortener.clicks.ClickLogged
import es.unizar.urlshortener.clicks.application.RecordClick
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class ClickRecorder(
    private val recordClick: RecordClick,
) {
    @ApplicationModuleListener
    fun on(event: ClickLogged) {
        recordClick.record(event)
    }
}
