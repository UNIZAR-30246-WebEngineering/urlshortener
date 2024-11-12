package es.unizar.urlshortener.infrastructure.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

/**
 * Specification of the repository of [ShortUrlEntity].
 *
 * **Note**: Spring Boot is able to discover this [JpaRepository] without further configuration.
 */
interface ShortUrlEntityRepository : JpaRepository<ShortUrlEntity, String> {
    /**
     * Finds a [ShortUrlEntity] by its hash.
     *
     * @param hash The hash of the [ShortUrlEntity].
     * @return The found [ShortUrlEntity] or null if not found.
     */
    fun findByHash(hash: String): ShortUrlEntity?
}

/**
 * Specification of the repository of [ClickEntity].
 *
 * **Note**: Spring Boot is able to discover this [JpaRepository] without further configuration.
 */
@Repository
interface ClickEntityRepository : JpaRepository<ClickEntity, Long> {
    /**
     * Finds all [ClickEntity] records within the specified time frame.
     *
     * @param start The start of the time frame.
     * @param end The end of the time frame.
     * @return A list of [ClickEntity] matching the time frame.
     */
    @Query("SELECT c FROM ClickEntity c WHERE c.created BETWEEN :start AND :end")
    fun findClicksByTimeFrame(
        @Param("start") start: OffsetDateTime,
        @Param("end") end: OffsetDateTime
    ): List<ClickEntity>
}
