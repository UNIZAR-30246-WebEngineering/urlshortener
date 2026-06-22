@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.sanitizeInput

/**
 * Use case for creating short URLs from long URLs.
 * 
 * This is a core business operation that:
 * 1. Validates the input URL format and safety
 * 2. Generates a unique hash for the URL
 * 3. Stores the mapping in the repository
 * 4. Returns the created short URL entity
 * 
 * This follows the **Use Case** pattern from Clean Architecture, encapsulating
 * business logic and coordinating between domain services.
 * 
 * @see <a href="https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html">Clean Architecture</a>
 */
interface CreateShortUrlUseCase {
    /**
     * Creates a short URL for the given URL and optional metadata.
     * 
     * The process involves:
     * 1. Input sanitization and validation
     * 2. URL format validation via ValidatorService
     * 3. Hash generation via HashService
     * 4. Domain entity creation with value objects
     * 5. Persistence via ShortUrlRepositoryService
     *
     * @param url The URL to be shortened (must be valid HTTP/HTTPS URL)
     * @param data Optional metadata (IP, sponsor, owner, etc.)
     * @return The created [ShortUrl] entity with generated hash
     * @throws InvalidUrlException if the URL format is invalid
     * @throws InvalidInputException if input validation fails
     */
    fun create(url: String, data: ShortUrlProperties): ShortUrl
}

/**
 * Implementation of [CreateShortUrlUseCase].
 */
class CreateShortUrlUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val validatorService: ValidatorService,
    private val hashService: HashService
) : CreateShortUrlUseCase {
    /**
     * Creates a short URL for the given URL and optional data.
     *
     * @param url The URL to be shortened.
     * @param data The optional properties for the short URL.
     * @return The created [ShortUrl] entity.
     * @throws InvalidUrlException if the URL is not valid.
     * @throws InvalidInputException if the input is invalid.
     */
    override fun create(url: String, data: ShortUrlProperties): ShortUrl {
        // Sanitize basic input constraints
        val sanitizedUrl = sanitizeInput(url, "url", InputLimits.MAX_URL_LENGTH)
        
        // Use validatorService for URL format validation
        return if (safeCall { validatorService.isValid(sanitizedUrl) }) {
            val id = safeCall { hashService.hashUrl(sanitizedUrl) }
            val su = ShortUrl(
                hash = UrlHash(id),
                redirection = Redirection(target = Url(sanitizedUrl)),
                properties = data
            )
            safeCall { shortUrlRepository.save(su) }
        } else {
            throw InvalidUrlException(sanitizedUrl)
        }
    }
}
