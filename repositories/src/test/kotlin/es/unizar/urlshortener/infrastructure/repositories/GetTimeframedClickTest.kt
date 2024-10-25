package es.unizar.urlshortener.infrastructure.repositories

import es.unizar.urlshortener.core.Click
import es.unizar.urlshortener.core.usecases.GetClickAnalyticsUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import java.time.OffsetDateTime

class GetTimeframedClickTest {

    private val clickEntityRepository: ClickEntityRepository = mock()
    private val getClickAnalyticsUseCase = GetClickAnalyticsUseCase(clickEntityRepository)

    @Test
    fun `should save clicks and retrieve them correctly within the timeframe`() {
        // Crear clicks de prueba
        val click1 = Click(
            id = null,
            hash = "hash1",
            created = OffsetDateTime.parse("2024-09-10T12:00:00Z"),
            ip = "192.168.1.1",
            referrer = "referrer1",
            browser = "browser1",
            platform = "platform1",
            country = "country1"
        )
        val click2 = Click(
            id = null,
            hash = "hash2",
            created = OffsetDateTime.parse("2024-09-25T12:00:00Z"),
            ip = "192.168.1.2",
            referrer = "referrer2",
            browser = "browser2",
            platform = "platform2",
            country = "country2"
        )
        val clickOutside = Click(
            id = null,
            hash = "hashOutside",
            created = OffsetDateTime.parse("2024-10-01T12:00:00Z"),
            ip = "192.168.1.3",
            referrer = "referrer3",
            browser = "browser3",
            platform = "platform3",
            country = "country3"
        )

        // Simular el comportamiento del repositorio para guardar clicks
        whenever(clickEntityRepository.save(any()))
            .thenReturn(click1) // Simula que se guarda click1

        // Guardar clicks en el repositorio
        getClickAnalyticsUseCase.saveClick(click1)
        getClickAnalyticsUseCase.saveClick(click2)

        // Simular la recuperación de clicks en un rango de tiempo
        whenever(clickEntityRepository.findClicksByTimeFrame(any(), any()))
            .thenReturn(listOf(click1, click2))

        // Definir el TimeFrame
        val timeFrameStart = OffsetDateTime.parse("2024-09-01T00:00:00Z")
        val timeFrameEnd = OffsetDateTime.parse("2024-09-30T23:59:59Z")

        // Llamar al caso de uso para obtener clicks
        val clicks = getClickAnalyticsUseCase.findClicksByTimeFrame(timeFrameStart, timeFrameEnd)

        // Verificar que los clicks devueltos son los esperados
        assertEquals(2, clicks.size)
        assertTrue(clicks.contains(click1))
        assertTrue(clicks.contains(click2))
        assertFalse(clicks.contains(clickOutside))

        // Verificar que el método de búsqueda fue llamado con los parámetros correctos
        verify(clickEntityRepository).findClicksByTimeFrame(timeFrameStart, timeFrameEnd)
    }
}
