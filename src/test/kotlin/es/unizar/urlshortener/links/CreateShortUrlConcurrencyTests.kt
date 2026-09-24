package es.unizar.urlshortener.links

import es.unizar.urlshortener.links.application.CreateShortUrl
import es.unizar.urlshortener.links.application.HashCollisionException
import es.unizar.urlshortener.links.application.HashGenerator
import es.unizar.urlshortener.links.application.LinkService.Companion.MAX_HASH_ATTEMPTS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Hashes are the URL's text after the last `/`, so tests choose collisions (and salted `#n` rehashes) explicitly. */
@SpringBootTest
@Import(CreateShortUrlConcurrencyTests.Hashing::class)
class CreateShortUrlConcurrencyTests {
    @Autowired
    lateinit var createShortUrl: CreateShortUrl

    @Test
    fun `concurrent creates of the same URL all return the same hash`() {
        val hash = UUID.randomUUID().toString()
        val url = "https://example.com/$hash"
        val results = runConcurrently(CREATES) { createShortUrl.create(url).hash }
        assertEquals(List(CREATES) { hash }, results)
    }

    @Test
    fun `a colliding URL gets the next salted hash, and keeps it on repeat`() {
        val hash = UUID.randomUUID().toString()
        val first = "https://example.com/$hash"
        val second = "https://example.org/$hash"
        assertEquals(hash, createShortUrl.create(first).hash)
        assertEquals("$hash#1", createShortUrl.create(second).hash)
        assertEquals("$hash#1", createShortUrl.create(second).hash)
        assertEquals(hash, createShortUrl.create(first).hash)
    }

    @Test
    fun `concurrent colliding URLs each get one distinct hash`() {
        val hash = UUID.randomUUID().toString()
        val urls = listOf("https://example.com/$hash", "https://example.org/$hash")
        val results =
            runConcurrently(CREATES) { index ->
                val url = urls[index % urls.size]
                url to createShortUrl.create(url).hash
            }
        val hashesByUrl = results.groupBy({ it.first }, { it.second }).mapValues { (_, hashes) -> hashes.toSet() }
        assertEquals(listOf(1, 1), hashesByUrl.values.map { it.size })
        assertEquals(setOf(hash, "$hash#1"), hashesByUrl.values.flatten().toSet())
    }

    @Test
    fun `a URL is rejected when all candidate hashes are taken`() {
        val hash = UUID.randomUUID().toString()
        (1..MAX_HASH_ATTEMPTS).forEach { createShortUrl.create("https://host$it.example.com/$hash") }
        assertThrows<HashCollisionException> { createShortUrl.create("https://other.example.com/$hash") }
    }

    private fun <T> runConcurrently(
        tasks: Int,
        action: (Int) -> T,
    ): List<T> {
        val pool = Executors.newFixedThreadPool(tasks)
        val start = CountDownLatch(1)
        val futures =
            (0 until tasks).map { index ->
                pool.submit<T> {
                    start.await()
                    action(index)
                }
            }
        start.countDown()
        return futures.map { it.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) }.also { pool.shutdown() }
    }

    private companion object {
        const val CREATES = 16
        const val TIMEOUT_SECONDS = 20L
    }

    @TestConfiguration
    class Hashing {
        @Bean
        @Primary
        fun pathSegmentHash() = HashGenerator { url -> url.substringAfterLast('/') }
    }
}
