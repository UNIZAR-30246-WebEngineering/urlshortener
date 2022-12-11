package es.unizar.urlshortener.infrastructure.repositories

import es.unizar.urlshortener.core.usecases.ClickSum
import es.unizar.urlshortener.core.usecases.UserClicks
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Specification of the repository of [ShortUrlEntity].
 *
 * **Note**: Spring Boot is able to discover this [JpaRepository] without further configuration.
 */
interface ShortUrlEntityRepository : JpaRepository<ShortUrlEntity, String> {
    fun findByHash(hash: String): ShortUrlEntity?
    @Query("SELECT owner AS user, COUNT(owner) AS sum FROM ShortUrlEntity GROUP BY owner ORDER BY owner DESC")
    fun computeUserClicks(): List<UserClicks>
}

/**
 * Specification of the repository of [ClickEntity].
 *
 * **Note**: Spring Boot is able to discover this [JpaRepository] without further configuration.
 */
interface ClickEntityRepository : JpaRepository<ClickEntity, Long> {
    /*@Query("SELECT id, COUNT FROM ClickEntity GROUP BY id ORDER BY id DESC")*/
    @Query("SELECT id AS hash, COUNT(id) AS sum FROM ClickEntity GROUP BY id ORDER BY id DESC")
    fun computeClickSum(): List<ClickSum>
}