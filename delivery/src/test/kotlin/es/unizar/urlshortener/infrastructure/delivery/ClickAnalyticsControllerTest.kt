package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.Click
import es.unizar.urlshortener.core.ClickRepositoryService
import es.unizar.urlshortener.core.TimeFrame
import es.unizar.urlshortener.core.usecases.GetClickAnalyticsUseCaseImpl
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import kotlin.test.Test
import java.time.OffsetDateTime



@WebMvcTest
@ContextConfiguration(
    classes = [
        ClickAnalyticsControllerImpl::class,
        RestResponseEntityExceptionHandler::class
    ]
)
class ClickAnalyticsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var clickRepositoryService: ClickRepositoryService

    @Test
    fun `getClickStats returns a list of clicks when there are clicks within the timeframe`() {
        // Configura los datos de prueba
        val start = 1609459200000L // Ejemplo de timestamp de inicio (1 enero 2021)
        val end = 1612137600000L   // Ejemplo de timestamp de fin (31 enero 2021)
        val timeFrame = TimeFrame(start, end)

        // Define algunos clicks de ejemplo
        val clicks = listOf(
            Click(hash = "abc123", created = OffsetDateTime.now()),
            Click(hash = "xyz789", created = OffsetDateTime.now())
        )

        // Configura el comportamiento del mock
        given(clickRepositoryService.findClicksByTimeFrame(timeFrame)).willReturn(clicks)

        // Realiza una solicitud GET y verifica la respuesta
        mockMvc.perform(get("/stats/{start}/{end}", start, end))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(clicks.size))
            .andExpect(jsonPath("$[0].hash").value("abc123"))
            .andExpect(jsonPath("$[1].hash").value("xyz789"))
    }

    @Test
    fun `getClickStats returns 404 when there are no clicks in the timeframe`() {
        // Configura los datos de prueba
        val start = 1609459200000L
        val end = 1612137600000L
        val timeFrame = TimeFrame(start, end)

        // Configura el mock para devolver una lista vacía
        given(clickRepositoryService.findClicksByTimeFrame(timeFrame)).willReturn(emptyList())

        // Realiza una solicitud GET y verifica la respuesta
        mockMvc.perform(get("/stats/{start}/{end}", start, end))
            .andExpect(status().isNotFound)
    }
}
