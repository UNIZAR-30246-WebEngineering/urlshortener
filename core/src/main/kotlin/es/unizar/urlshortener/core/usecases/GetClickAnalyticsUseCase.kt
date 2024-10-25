@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import java.time.OffsetDateTime
import es.unizar.urlshortener.core.usecases.GetClickAnalyticsUseCaseImpl.ClickAnalytics
import es.unizar.urlshortener.core.usecases.GetClickAnalyticsUseCaseImpl.ClickFilters

/**
 * Provides consolidated click data over a specified time frame.
 * Includes information about the Browser, Referrer, Country, and Platform.
 * Allows filtering clicks by any of these parameters.
 */
interface GetClickAnalyticsUseCase {
    /**
     * Retrieves click data within a specified time frame.
     *
     * @param timeFrame The range of time (minute, hour, or day).
     * @param filters Optional filters such as Browser, Referrer, Country, or Platform.
     * @return A list of [ClickAnalytics] entities.
     */
    fun getClicks(timeFrame: es.unizar.urlshortener.core.usecases.TimeFrame, filters: ClickFilters):
            List<ClickAnalytics>
}

data class TimeFrame(
    val start: OffsetDateTime,
    val end: OffsetDateTime
)

/**
 * Implementation of [GetClickAnalyticsUseCase].
 */
class GetClickAnalyticsUseCaseImpl(
    private val clickRepository: ClickRepositoryService
    ) : GetClickAnalyticsUseCase {
    /**
     * Retrieves click data within a specified time frame, applying the given filters.
     *
     * @param timeFrame The range of time (minute, hour, or day).
     * @param filters Optional filters such as Browser, Referrer, Country, or Platform.
     * @return A list of [ClickAnalytics] entities.
     */
    override fun getClicks(timeFrame: es.unizar.urlshortener.core.usecases.TimeFrame, filters: ClickFilters):
            List<ClickAnalytics> {
        // Retrieve the clicks from the repository for the given time frame
        val clicks = clickRepository.findClicksByTimeFrame(timeFrame)

        // Apply filters to the result set
        val filteredClicks = clicks.filter {
            (filters.browser == null || it.properties.browser == filters.browser) &&
                    (filters.referrer == null || it.properties.referrer == filters.referrer) &&
                    (filters.country == null || it.properties.country == filters.country) &&
                    (filters.platform == null || it.properties.platform == filters.platform)
        }

        // Convert Click to ClickAnalytics
        return filteredClicks.map { click ->
            ClickAnalytics(
                timestamp = click.created.toEpochSecond(),
                browser = click.properties.browser.toString(),
                referrer = click.properties.referrer.toString(),
                country = click.properties.country.toString(),
                platform = click.properties.platform.toString()
            )
        }
    }

    /**
     * Represents a time frame for retrieving click data.
     */
    data class TimeFrame(
        val start: Long,
        val end: Long
    )

    /**
     * Represents filters for click analytics.
     */
    data class ClickFilters(
        val browser: String? = null,
        val referrer: String? = null,
        val country: String? = null,
        val platform: String? = null
    )

    /**
     * Represents click analytics data.
     */
    data class ClickAnalytics(
        val timestamp: Long,
        val browser: String,
        val referrer: String,
        val country: String,
        val platform: String
    )
}
