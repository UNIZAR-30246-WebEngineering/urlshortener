package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.InternalError
import es.unizar.urlshortener.core.InvalidInputException
import es.unizar.urlshortener.core.InvalidUrlException
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.ValidatorService
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateShortUrlUseCaseTest {

    @Test
    fun `creates returns a basic redirect if it can compute a hash`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(hashService.hashUrl("http://example.com/")).thenReturn("f684a3c4")
        whenever(shortUrlRepository.save(any())).doAnswer { it.arguments[0] as ShortUrl }

        val createShortUrlUseCase = CreateShortUrlUseCaseImpl(shortUrlRepository, validatorService, hashService)
        val shortUrl = createShortUrlUseCase.create("http://example.com/", shortUrlProperties)

        assertEquals(shortUrl.hash.value, "f684a3c4")
    }

    @Test
    fun `creates returns invalid URL exception if the URL is not valid`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()

        whenever(validatorService.isValid("ftp://example.com/")).thenReturn(false)

        val createShortUrlUseCase = CreateShortUrlUseCaseImpl(shortUrlRepository, validatorService, hashService)

        assertFailsWith<InvalidUrlException> {
            createShortUrlUseCase.create("ftp://example.com/", shortUrlProperties)
        }
    }

    @Test
    fun `creates returns invalid URL exception if the URI cannot be validated`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()

        whenever(validatorService.isValid("http://example.com/")).thenThrow(RuntimeException())

        val createShortUrlUseCase = CreateShortUrlUseCaseImpl(shortUrlRepository, validatorService, hashService)

        assertFailsWith<InternalError> {
            createShortUrlUseCase.create("http://example.com/", shortUrlProperties)
        }
    }

    @Test
    fun `creates returns invalid URL exception if the hash cannot be computed`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(hashService.hashUrl("http://example.com/")).thenThrow(RuntimeException())

        val createShortUrlUseCase = CreateShortUrlUseCaseImpl(shortUrlRepository, validatorService, hashService)

        assertFailsWith<InternalError> {
            createShortUrlUseCase.create("http://example.com/", shortUrlProperties)
        }
    }

    @Test
    fun `creates returns invalid URL exception if the short URL cannot be saved`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()

        whenever(validatorService.isValid("http://example.com/")).thenReturn(true)
        whenever(hashService.hashUrl("http://example.com/")).thenReturn("f684a3c4")
        whenever(shortUrlRepository.save(any())).thenThrow(RuntimeException())

        val createShortUrlUseCase = CreateShortUrlUseCaseImpl(shortUrlRepository, validatorService, hashService)

        assertFailsWith<InternalError> {
            createShortUrlUseCase.create("http://example.com/", shortUrlProperties)
        }
    }

    @Test
    fun `creates throws InvalidInputException for blank URL`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()

        val createShortUrlUseCase = CreateShortUrlUseCaseImpl(shortUrlRepository, validatorService, hashService)

        assertFailsWith<InvalidInputException> {
            createShortUrlUseCase.create("   ", shortUrlProperties)
        }
    }

    @Test
    fun `creates throws InvalidUrlException for URL with invalid characters`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()

        // Mock validatorService to return false for invalid URL with control characters
        whenever(validatorService.isValid("http://example.com/\u0000")).thenReturn(false)

        val createShortUrlUseCase = CreateShortUrlUseCaseImpl(shortUrlRepository, validatorService, hashService)

        assertFailsWith<InvalidUrlException> {
            createShortUrlUseCase.create("http://example.com/\u0000", shortUrlProperties)
        }
    }

    @Test
    fun `creates throws InvalidUrlException for URL with URL-encoded invalid characters`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()

        // Mock validatorService to return false for URL with %20 (space) and other invalid encoded chars
        whenever(validatorService.isValid("http://example.com/%20invalid%00path")).thenReturn(false)

        val createShortUrlUseCase = CreateShortUrlUseCaseImpl(shortUrlRepository, validatorService, hashService)

        assertFailsWith<InvalidUrlException> {
            createShortUrlUseCase.create("http://example.com/%20invalid%00path", shortUrlProperties)
        }
    }

    @Test
    fun `creates throws InvalidInputException for URL exceeding max length`() {
        val shortUrlRepository = mock<ShortUrlRepositoryService>()
        val validatorService = mock<ValidatorService>()
        val hashService = mock<HashService>()
        val shortUrlProperties = mock<ShortUrlProperties>()
        val longUrl = "http://example.com/" + "a".repeat(3000)

        val createShortUrlUseCase = CreateShortUrlUseCaseImpl(shortUrlRepository, validatorService, hashService)

        assertFailsWith<InvalidInputException> {
            createShortUrlUseCase.create(longUrl, shortUrlProperties)
        }
    }
}
