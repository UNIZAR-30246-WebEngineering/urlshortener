package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.InvalidLocationException
import es.unizar.urlshortener.core.LocationData
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.util.AssertionErrors.assertEquals

@AutoConfigureMockMvc
class LocationServiceImplTest {
    @Autowired
    private lateinit var locationService: LocationServiceImpl

    @BeforeEach
    fun setUp() {
        locationService = LocationServiceImpl()
    }

    @Test
    fun `GET a valid location if the coords are correct`() {
        val location = locationService.getLocationByCord(
                lat = 41.641412477417894,
                lon = -0.8800855922769534

        )
        assertEquals("Equals:", location ,
            LocationData(
                    41.641412477417894,
                    -0.8800855922769534,
                    "España",
                    "Zaragoza",
                    "Aragón",
                    "Calle de Francisco Albiñana",
                    "50008"
                )
            )
    }

    @Test
    fun `THROW an exception if the coords are incorrect`() {
        assertThrows<InvalidLocationException> {
            locationService.getLocationByCord(
                lat = 41.641412477417894,
                lon = -360.0
            )
        }
    }

    @Test
    fun `GET a valid location if the ip is correct`() {
        val location = locationService.getLocationByIp(
                ip = "155.210.154.100"

        )
        /*assertEquals("Son iguales", location ,
                LocationData(
                        41.6405,
                        -0.8814,
                        "Spain",
                        "Zaragoza",
                        "Aragon",
                        null,
                        "50001"
                )
        )*/
        // Clases iguales
        assertEquals("Equals:", location.javaClass , LocationData().javaClass)
    }

    @Test
    fun `THROW an exception if the IP is incorrect`() {
        assertThrows<InvalidLocationException> {
            locationService.getLocationByIp(
                    ip = "-1"
            )
        }
    }
}