package es.unizar.urlshortener.core

/**
 * Core service interfaces (ports) for the URL Shortener application.
 * 
 * This file defines the **ports** in the Hexagonal Architecture pattern, which represent
 * the interfaces that the core domain uses to interact with external systems. These ports
 * define the contracts that adapters must implement, ensuring that the core domain remains
 * independent of external dependencies.
 * 
 * **Key Concepts:**
 * - **Ports**: Interfaces that define what the core needs from external systems
 * - **Adapters**: Implementations of these ports in the infrastructure layer
 * - **Dependency Inversion**: Core depends on abstractions, not concrete implementations
 * - **Testability**: Easy to mock these interfaces for unit testing
 * 
 * @see <a href="https://alistair.cockburn.us/hexagonal-architecture/">Hexagonal Architecture</a>
 */

/**
 * Repository port for managing [Click] entities.
 * 
 * This interface defines the contract for persisting click tracking data.
 * It follows the Repository pattern, providing a clean abstraction over
 * data storage mechanisms.
 * 
 * **Design Benefits:**
 * - **Abstraction**: Core doesn't know about specific database technology
 * - **Testability**: Easy to create in-memory implementations for testing
 * - **Flexibility**: Can switch between different storage backends
 * 
 * @see <a href="https://martinfowler.com/eaaCatalog/repository.html">Repository Pattern</a>
 */
interface ClickRepositoryService {
    /**
     * Persists a click event to the repository.
     * 
     * This method saves click tracking data including user information,
     * timestamps, and metadata for analytics purposes.
     *
     * @param cl The [Click] entity containing all tracking information
     * @return The persisted [Click] entity (may include generated IDs)
     * @throws InternalError if persistence fails due to infrastructure issues
     */
    fun save(cl: Click): Click
}

/**
 * Repository port for managing [ShortUrl] entities.
 * 
 * This interface defines the contract for persisting and retrieving short URL
 * mappings. It's the primary data access interface for the core domain.
 * 
 * **Key Responsibilities:**
 * - Store URL hash to target URL mappings
 * - Retrieve short URLs by their hash keys
 * - Handle concurrent access safely
 * 
 * **Performance Considerations:**
 * - Hash-based lookups should be O(1) for optimal performance
 * - Consider indexing strategies for large datasets
 */
interface ShortUrlRepositoryService {
    /**
     * Retrieves a short URL by its hash key.
     * 
     * This is the primary lookup method used during redirection.
     * Performance is critical as this method is called for every
     * short URL access.
     *
     * @param id The [UrlHash] key to search for
     * @return The matching [ShortUrl] entity or null if not found
     * @throws InternalError if database access fails
     */
    fun findByKey(id: UrlHash): ShortUrl?

    /**
     * Persists a short URL mapping to the repository.
     * 
     * This method stores the complete short URL entity including
     * the hash, target URL, and metadata.
     *
     * @param su The [ShortUrl] entity to persist
     * @return The persisted [ShortUrl] entity (may include generated fields)
     * @throws InternalError if persistence fails
     */
    fun save(su: ShortUrl): ShortUrl
}

/**
 * Service port for URL validation.
 * 
 * This interface abstracts URL validation logic, allowing the core to
 * validate URLs without depending on specific validation libraries.
 * 
 * **Design Decision**: This could be part of the core domain, but extracting
 * it as a port allows for:
 * - **Flexibility**: Different validation strategies (strict vs. permissive)
 * - **Testing**: Easy to mock for unit tests
 * - **Extensibility**: Can add complex validation rules without changing core
 * 
 * **Validation Criteria:**
 * - URL format compliance (RFC 3986)
 * - Supported protocols (HTTP/HTTPS)
 * - Security considerations (malicious URLs)
 */
interface ValidatorService {
    /**
     * Validates whether a URL can be shortened.
     * 
     * This method performs comprehensive URL validation including
     * format checking, protocol validation, and security screening.
     *
     * @param url The URL string to validate
     * @return true if the URL is valid and safe to shorten, false otherwise
     * @throws InternalError if validation service is unavailable
     */
    fun isValid(url: String): Boolean
}

/**
 * Service port for URL hashing.
 * 
 * This interface abstracts hash generation, allowing the core to create
 * unique identifiers for URLs without depending on specific hashing
 * algorithms or libraries.
 * 
 * **Design Decision**: This could be part of the core domain, but extracting
 * it as a port allows for:
 * - **Algorithm Flexibility**: Can switch between MD5, SHA-256, custom algorithms
 * - **Collision Handling**: Different strategies for hash collisions
 * - **Performance Tuning**: Optimize for specific use cases
 * 
 * **Hash Requirements:**
 * - **Uniqueness**: Minimize collision probability
 * - **Consistency**: Same URL always produces same hash
 * - **Length**: Balance between uniqueness and URL length
 * - **Security**: Consider if hashes should be predictable
 */
interface HashService {
    /**
     * Generates a unique hash for the given URL.
     * 
     * This method creates a deterministic hash that serves as the
     * unique identifier for the short URL. The hash should be:
     * - Consistent for the same input
     * - Sufficiently unique to avoid collisions
     * - Short enough for user-friendly URLs
     *
     * @param url The URL to generate a hash for
     * @return A unique hash string for the URL
     * @throws InternalError if hashing service fails
     */
    fun hashUrl(url: String): String
}
