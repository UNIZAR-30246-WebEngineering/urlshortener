package es.unizar.urlshortener.clicks.application

import es.unizar.urlshortener.clicks.ClickLogged
import org.jmolecules.architecture.hexagonal.PrimaryPort
import org.jmolecules.architecture.hexagonal.SecondaryPort
import java.time.Instant

data class Click(
    val hash: String,
    val clientIp: String?,
    val occurredAt: Instant,
)

@SecondaryPort
fun interface ClickStore {
    fun save(click: Click)
}

@PrimaryPort
fun interface RecordClick {
    fun record(event: ClickLogged)
}
