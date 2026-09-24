package es.unizar.urlshortener.clicks.adapters.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface JpaClickRepository : JpaRepository<ClickEntity, Long> {
    fun countByHash(hash: String): Long
}
