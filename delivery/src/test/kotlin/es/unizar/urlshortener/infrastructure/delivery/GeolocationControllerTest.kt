package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.GeolocationData
import es.unizar.urlshortener.core.usecases.GeolocationUseCase
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest(GeolocationController::class)
@ContextConfiguration(classes = [GeolocationController::class])
class GeolocationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var geolocationUseCase: GeolocationUseCase

    @Test
    fun `returns geolocation data for a valid IP`() {
        given(geolocationUseCase.getGeolocation("8.8.8.8"))
            .willReturn(GeolocationData("United States", "Mountain View", "California"))

        mockMvc.perform(get("/geolocation/8.8.8.8"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.country").value("United States"))
            .andExpect(jsonPath("$.city").value("Mountain View"))
            .andExpect(jsonPath("$.region").value("California"))
    }

    @Test
    fun `returns 404 for an invalid IP`() {
        given(geolocationUseCase.getGeolocation("127.0.0.1")).willReturn(null)

        mockMvc.perform(get("/geolocation/127.0.0.1"))
            .andExpect(status().isNotFound)
    }
}
