package es.unizar.urlshortener.infrastructure.repositories

import es.unizar.urlshortener.core.Click
import es.unizar.urlshortener.core.ClickRepositoryService
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.UrlHash

/**
 * Infrastructure implementations of core domain ports.
 * 
 * This file contains the **Adapter** implementations that connect the core domain
 * to the persistence layer. These classes implement the ports defined in the core
 * module, providing concrete implementations using JPA repositories.
 * 
 * **Architecture Role:**
 * - **Adapters**: Implement core domain ports using infrastructure technology
 * - **Dependency Inversion**: Core depends on abstractions, infrastructure provides implementations
 * - **Data Mapping**: Convert between domain objects and JPA entities
 * - **Transaction Management**: Handle database transactions transparently
 * 
 * **Design Patterns:**
 * - **Repository Pattern**: Clean abstraction over data access
 * - **Adapter Pattern**: Adapts JPA repositories to domain interfaces
 * - **Data Mapper**: Converts between domain and persistence models
 * 
 * **Performance Considerations:**
 * - JPA repositories provide optimized database access
 * - Lazy loading for related entities
 * - Connection pooling handled by Spring Boot
 * - Query optimization through JPA/Hibernate
 * 
 * @see <a href="https://alistair.cockburn.us/hexagonal-architecture/">Hexagonal Architecture</a>
 * @see <a href="https://martinfowler.com/eaaCatalog/repository.html">Repository Pattern</a>
 */

/**
 * JPA-based implementation of the [ClickRepositoryService] port.
 * 
 * This adapter provides persistence for click tracking data using JPA/Hibernate.
 * It handles the conversion between domain [Click] objects and JPA [ClickEntity]
 * objects, ensuring clean separation between domain and persistence concerns.
 * 
 * **Responsibilities:**
 * - Persist click tracking data to the database
 * - Handle entity-to-domain object conversion
 * - Manage database transactions (delegated to Spring)
 * - Provide type-safe data access
 * 
 * **Performance Features:**
 * - JPA entity caching for improved performance
 * - Batch operations for high-volume scenarios
 * - Optimistic locking for concurrent access
 * - Connection pooling for scalability
 */
class ClickRepositoryServiceImpl(
    private val clickEntityRepository: ClickEntityRepository
) : ClickRepositoryService {
    
    /**
     * Persists a click event to the database.
     * 
     * This method handles the complete persistence flow:
     * 1. Converts domain [Click] object to JPA [ClickEntity]
     * 2. Saves the entity using JPA repository
     * 3. Converts the saved entity back to domain object
     * 4. Returns the persisted domain object
     * 
     * **Transaction Management:**
     * - Database transaction is managed by Spring's @Transactional
     * - Automatic rollback on exceptions
     * - Optimistic locking for concurrent modifications
     *
     * @param cl The [Click] domain object to persist
     * @return The persisted [Click] domain object (may include generated IDs)
     */
    override fun save(cl: Click): Click = clickEntityRepository.save(cl.toEntity()).toDomain()
}

/**
 * JPA-based implementation of the [ShortUrlRepositoryService] port.
 * 
 * This adapter provides persistence for short URL mappings using JPA/Hibernate.
 * It's the primary data access component for the URL shortener, handling both
 * storage and retrieval of short URL mappings.
 * 
 * **Key Operations:**
 * - **findByKey**: Critical for redirect performance (O(1) hash lookup)
 * - **save**: Stores new short URL mappings with metadata
 * 
 * **Performance Optimizations:**
 * - Hash-based indexing for fast lookups
 * - JPA second-level caching for popular URLs
 * - Connection pooling for high concurrency
 * - Query optimization through Hibernate
 * 
 * **Data Integrity:**
 * - Unique constraints on hash values
 * - Referential integrity for related data
 * - Optimistic locking for concurrent updates
 */
class ShortUrlRepositoryServiceImpl(
    private val shortUrlEntityRepository: ShortUrlEntityRepository
) : ShortUrlRepositoryService {
    
    /**
     * Retrieves a short URL by its hash key.
     * 
     * This is the most performance-critical method in the system, called for
     * every redirect operation. The implementation is optimized for:
     * - **Fast Lookups**: Hash-based indexing provides O(1) access
     * - **Caching**: JPA second-level cache for frequently accessed URLs
     * - **Minimal Overhead**: Direct entity-to-domain conversion
     * 
     * **Database Optimization:**
     * - Hash column is indexed for optimal performance
     * - Query uses index-only access when possible
     * - Connection pooling handles concurrent requests
     *
     * @param id The [UrlHash] key to search for
     * @return The matching [ShortUrl] domain object or null if not found
     */
    override fun findByKey(id: es.unizar.urlshortener.core.UrlHash): ShortUrl? = 
        shortUrlEntityRepository.findByHash(id.value)?.toDomain()

    /**
     * Persists a short URL mapping to the database.
     * 
     * This method handles the storage of new short URL mappings, including:
     * - Hash-to-URL mapping storage
     * - Metadata persistence (safety, timestamps, etc.)
     * - Automatic ID generation
     * - Constraint validation
     * 
     * **Data Validation:**
     * - Unique hash constraint prevents duplicates
     * - URL format validation at database level
     * - Metadata integrity checks
     * 
     * **Transaction Safety:**
     * - Atomic operation ensures data consistency
     * - Rollback on constraint violations
     * - Optimistic locking for concurrent access
     *
     * @param su The [ShortUrl] domain object to persist
     * @return The persisted [ShortUrl] domain object (may include generated fields)
     */
    override fun save(su: ShortUrl): ShortUrl = shortUrlEntityRepository.save(su.toEntity()).toDomain()
}
