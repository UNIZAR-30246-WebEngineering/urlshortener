package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.Click
import es.unizar.urlshortener.core.ClickFilters
import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ClickRepositoryService
import es.unizar.urlshortener.core.TimeFrame
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import java.time.OffsetDateTime


class GetClickAnalyticsUseCaseTest {

    @Test
    fun `getClicks returns a JSON map if it can retrieve clicks in timeframe`() {
        val repository = mock<ClickRepositoryService>()
        val useCase = GetClickAnalyticsUseCaseImpl(repository)

        // Prepare test data
        // We generate offsetDatetime with the furst 2 functions, and then convert it to a long data with the last 2
        val startTime = OffsetDateTime.now().minusDays(1).toInstant().toEpochMilli()
        val endTime = OffsetDateTime.now().toInstant().toEpochMilli()
        val timeFrame = TimeFrame(startTime, endTime)

        // Create mock Click data
        val clickProperties = ClickProperties(browser = "Chrome", referrer = "http://example.com", country = "US",
            platform = "Windows")
        val mockClick = mock<Click>() // Asumiendo que hay una clase Click
        whenever(mockClick.properties).thenReturn(clickProperties)
        whenever(mockClick.created).thenReturn(OffsetDateTime.now())

        // Set up repository to return mock clicks
        whenever(repository.findClicksByTimeFrame(any())).thenReturn(listOf(mockClick))

        // Call the use case
        val result = useCase.getClicks(timeFrame, ClickFilters())

        // Assert the result
        assertEquals(1, result.size)
        assertEquals("Chrome", result[0].browser)
        assertEquals("http://example.com", result[0].referrer)
        assertEquals("US", result[0].country)
        assertEquals("Windows", result[0].platform)
    }
    //Not testable from here, need to activate database
//    @Test
//    fun `getClicks does not return clicks outside the timeframe`() {
//        val repository = mock<ClickRepositoryService>()
//        val clickProperties = mock<ClickProperties>()
//
//        // Set up time frame
//        val timeFrame = TimeFrame(
//            start = OffsetDateTime.parse("2024-08-25T00:00:00Z"),
//            end = OffsetDateTime.parse("2024-09-25T23:59:59Z")
//        )
//
//        // Sample clicks outside timeframe
//        val clickOutside = Click(
//            created = OffsetDateTime.parse("2024-10-24T12:00:00Z"),
//            properties = clickProperties,
//            hash = "hashOutside"  // Añadir hash
//        )
//
//        // Mock the repository to return clicks including one outside timeframe
//        whenever(repository.findClicksByTimeFrame(timeFrame)).thenReturn(listOf(clickOutside))
//
//        val useCase = GetClickAnalyticsUseCaseImpl(repository)
//        val clicks = useCase.getClicks(timeFrame, ClickFilters())
//
//        // Assert that no clicks are returned
//        assertEquals(0, clicks.size)
//    }
}
