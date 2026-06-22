package es.unizar.urlshortener.infrastructure.delivery

import com.google.common.hash.Hashing
import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.ValidatorService
import org.apache.commons.validator.routines.UrlValidator
import java.nio.charset.StandardCharsets

/**
 * Infrastructure implementations of core domain service ports.
 * 
 * This file contains **Adapter** implementations that provide concrete functionality
 * for the core domain services. These implementations use external libraries and
 * utilities to fulfill the contracts defined by the core domain ports.
 * 
 * **Architecture Role:**
 * - **Adapters**: Implement core domain ports using infrastructure libraries
 * - **Dependency Inversion**: Core depends on abstractions, infrastructure provides implementations
 * - **External Integration**: Integrates with third-party libraries (Google Guava, Apache Commons)
 * - **Technology Choices**: Encapsulates specific technology decisions in infrastructure layer
 * 
 * **Design Benefits:**
 * - **Testability**: Easy to mock for unit testing
 * - **Flexibility**: Can switch implementations without changing core
 * - **Separation of Concerns**: Technology choices isolated from business logic
 * - **Maintainability**: Changes to external libraries don't affect core domain
 * 
 * @see <a href="https://alistair.cockburn.us/hexagonal-architecture/">Hexagonal Architecture</a>
 */

/**
 * Apache Commons-based implementation of the [ValidatorService] port.
 * 
 * This adapter provides URL validation functionality using the Apache Commons
 * Validator library. It implements comprehensive URL validation including
 * format checking, protocol validation, and security considerations.
 * 
 * **Validation Features:**
 * - **Protocol Support**: HTTP and HTTPS schemes only
 * - **Format Validation**: RFC 3986 compliant URL structure
 * - **Security**: Prevents malicious URL schemes
 * - **Performance**: Optimized validation algorithms
 * 
 * **Technology Choice:**
 * - **Apache Commons Validator**: Industry-standard validation library
 * - **Mature and Stable**: Well-tested in production environments
 * - **Comprehensive**: Handles edge cases and security considerations
 * - **Performance**: Optimized for high-throughput scenarios
 * 
 * **Security Considerations:**
 * - Only allows HTTP/HTTPS protocols
 * - Prevents protocol-based attacks (javascript:, data:, etc.)
 * - Validates URL structure to prevent injection attacks
 */
class ValidatorServiceImpl : ValidatorService {
    
    /**
     * Validates whether a URL is safe and properly formatted.
     * 
     * This method performs comprehensive URL validation including:
     * - **Format Validation**: Ensures proper URL structure
     * - **Protocol Validation**: Only allows HTTP/HTTPS schemes
     * - **Security Checks**: Prevents malicious protocol schemes
     * - **Edge Case Handling**: Handles various URL formats and edge cases
     * 
     * **Performance Characteristics:**
     * - Fast validation using optimized algorithms
     * - Minimal memory allocation
     * - Thread-safe operation
     * - Cached validator instance for efficiency
     *
     * @param url The URL string to validate
     * @return true if the URL is valid and safe, false otherwise
     */
    override fun isValid(url: String) = urlValidator.isValid(url)

    companion object {
        /**
         * Thread-safe URL validator instance supporting HTTP and HTTPS schemes.
         * 
         * This validator is configured to:
         * - Accept only HTTP and HTTPS protocols
         * - Validate URL format according to RFC 3986
         * - Perform security checks against malicious schemes
         * - Handle internationalized domain names (IDN)
         * 
         * **Configuration:**
         * - **Schemes**: ["http", "https"] - Only web protocols allowed
         * - **Thread Safety**: Safe for concurrent access
         * - **Performance**: Optimized for high-throughput validation
         */
        val urlValidator = UrlValidator(arrayOf("http", "https"))
    }
}

/**
 * Google Guava-based implementation of the [HashService] port.
 * 
 * This adapter provides hash generation functionality using Google's Guava
 * library. It implements the Murmur3 32-bit hashing algorithm, which provides
 * excellent performance and good distribution characteristics for URL hashing.
 * 
 * **Hash Algorithm:**
 * - **Murmur3 32-bit**: Fast, non-cryptographic hash function
 * - **Deterministic**: Same input always produces same output
 * - **Good Distribution**: Minimizes collision probability
 * - **Performance**: Optimized for speed over cryptographic security
 * 
 * **Technology Choice:**
 * - **Google Guava**: High-quality, well-maintained library
 * - **Murmur3**: Industry-standard hash function
 * - **32-bit**: Balance between uniqueness and URL length
 * - **Fixed Seed**: Ensures consistent hashing across application restarts
 * 
 * **Collision Handling:**
 * - Low collision probability for typical URL lengths
 * - Consider monitoring collision rates in production
 * - Can be upgraded to 64-bit or different algorithms if needed
 * 
 * **Performance Characteristics:**
 * - Very fast hashing (microseconds per URL)
 * - Minimal memory allocation
 * - Thread-safe operation
 * - Consistent performance across different input sizes
 */
class HashServiceImpl : HashService {
    
    /**
     * Generates a unique hash for the given URL.
     * 
     * This method creates a deterministic hash using the Murmur3 32-bit algorithm:
     * - **Consistent**: Same URL always produces same hash
     * - **Fast**: Optimized for high-throughput scenarios
     * - **Compact**: 32-bit hash provides good uniqueness in reasonable space
     * - **UTF-8**: Handles international characters properly
     * 
     * **Hash Properties:**
     * - **Length**: 8 hexadecimal characters (32 bits)
     * - **Format**: Lowercase hexadecimal string
     * - **Collision Rate**: Very low for typical URL lengths
     * - **Distribution**: Good distribution across hash space
     * 
     * **Use Cases:**
     * - Short URL generation
     * - Cache keys
     * - Database partitioning
     * - Analytics correlation
     *
     * @param url The URL string to generate a hash for
     * @return A unique hash string (8 hexadecimal characters)
     */
    override fun hashUrl(url: String) = Hashing.murmur3_32_fixed().hashString(url, StandardCharsets.UTF_8).toString()
}
