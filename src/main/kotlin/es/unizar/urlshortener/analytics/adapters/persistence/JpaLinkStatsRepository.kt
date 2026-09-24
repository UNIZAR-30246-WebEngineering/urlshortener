package es.unizar.urlshortener.analytics.adapters.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface JpaLinkStatsRepository : JpaRepository<LinkStatsEntity, String>
