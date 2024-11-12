package es.unizar.urlshortener.infrastructure.repositories

import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@SpringBootApplication
open class TestApplication

@DataJpaTest
open class GetTimeframedClickTestDB {

    @Autowired
    private lateinit var clickEntityRepository: ClickEntityRepository

    @Test
    fun `should find clicks within the specified timeframe`() {
        // Insert 3 clicks: one before, one within, and one after the timeframe
        clickEntityRepository.save(
            ClickEntity(
                id = null,
                hash = "hash1",
                created = OffsetDateTime.of(2023, 10, 1, 10, 0, 0, 0, ZoneOffset.UTC),
                ip = "192.168.1.1",
                referrer = "referrer1",
                browser = "Chrome",
                platform = "Windows",
                country = "ES"
            )
        )

        clickEntityRepository.save(
            ClickEntity(
                id = null,
                hash = "hash2",
                created = OffsetDateTime.of(2023, 10, 2, 10, 0, 0, 0, ZoneOffset.UTC),
                ip = "192.168.1.2",
                referrer = "referrer2",
                browser = "Firefox",
                platform = "Linux",
                country = "FR"
            )
        )

        clickEntityRepository.save(
            ClickEntity(
                id = null,
                hash = "hash3",
                created = OffsetDateTime.of(2023, 10, 3, 10, 0, 0, 0, ZoneOffset.UTC),
                ip = "192.168.1.3",
                referrer = "referrer3",
                browser = "Safari",
                platform = "macOS",
                country = "US"
            )
        )
        // Define timeframe from Oct 2, 9:00 to Oct 2, 11:00
        val start = OffsetDateTime.of(2023, 10, 2, 9, 0, 0, 0, ZoneOffset.UTC)
        val end = OffsetDateTime.of(2023, 10, 2, 11, 0, 0, 0, ZoneOffset.UTC)

        // Query clicks within the timeframe
        val result = clickEntityRepository.findClicksByTimeFrame(start, end)

        // Assert that only one click (hash2) is returned
        assertEquals(1, result.size)
        assertEquals("hash2", result[0].hash)
    }
}
