package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.Click
import es.unizar.urlshortener.core.ClickRepositoryService
import es.unizar.urlshortener.core.usecases.GetClickAnalyticsUseCaseImpl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

interface ClickAnalyticsController {

    /**
     * Devuelve estadísticas de clics en el intervalo de tiempo definido por [start] y [end].
     */
    fun getClickStats(start: Long, end: Long): ResponseEntity<List<Click>>
}

@RestController
class ClickAnalyticsControllerImpl(
    private val clickRepositoryService: ClickRepositoryService
) : ClickAnalyticsController {

    @GetMapping("/stats/{start}/{end}")
    override fun getClickStats(
        @PathVariable start: Long,
        @PathVariable end: Long
    ): ResponseEntity<List<Click>> {
        val timeFrame = GetClickAnalyticsUseCaseImpl.TimeFrame(start, end)
        val clicks = clickRepositoryService.findClicksByTimeFrame(timeFrame)
        return if (clicks.isNotEmpty()) {
            ResponseEntity.ok(clicks)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}