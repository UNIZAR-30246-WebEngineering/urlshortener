package es.unizar.urlshortener.links.adapters.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface JpaShortUrlRepository : JpaRepository<ShortUrlEntity, String> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        insert into ShortUrlEntity (hash, target, createdAt)
        values (:hash, :target, :createdAt)
        on conflict (hash) do update set target = target
        """,
    )
    fun insertIfAbsent(
        @Param("hash") hash: String,
        @Param("target") target: String,
        @Param("createdAt") createdAt: Instant,
    ): Int
}
