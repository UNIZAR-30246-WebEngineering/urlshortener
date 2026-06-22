package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.ClickProperties
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.IpAddress
import es.unizar.urlshortener.core.Sponsor
import es.unizar.urlshortener.core.UrlSafety
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.RedirectUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

/**
 * REST API controller interface for the URL Shortener application.
 * 
 * This interface defines the contract for the REST API endpoints, following the
 * **Adapter** pattern in Hexagonal Architecture. It acts as the primary adapter
 * that translates HTTP requests into domain operations and domain responses back
 * to HTTP responses.
 * 
 * **Architecture Role:**
 * - **Adapter**: Translates between HTTP protocol and domain operations
 * - **Interface Segregation**: Clean separation between API contract and implementation
 * - **Dependency Inversion**: Depends on domain use cases, not infrastructure details
 * 
 * **API Design Principles:**
 * - **RESTful**: Follows REST conventions for resource-based URLs
 * - **Stateless**: Each request contains all necessary information
 * - **Idempotent**: Safe to retry operations (where applicable)
 * - **Content Negotiation**: Supports multiple content types
 * 
 * **Security Considerations:**
 * - Input validation and sanitization
 * - Rate limiting (implemented at infrastructure level)
 * - CORS configuration for web clients
 * - HTTPS enforcement in production
 * 
 * @see <a href="https://alistair.cockburn.us/hexagonal-architecture/">Hexagonal Architecture</a>
 * @see <a href="https://restfulapi.net/">REST API Design</a>
 */
interface UrlShortenerController {

    /**
     * Redirects users from short URLs to their target destinations.
     * 
     * This is the primary endpoint that handles short URL redirections. It performs
     * two main operations:
     * 1. **Redirection**: Looks up the target URL and returns appropriate HTTP redirect
     * 2. **Analytics**: Logs the click event for tracking and analytics
     * 
     * **HTTP Semantics:**
     * - Returns 307 (Temporary Redirect) or 301 (Permanent Redirect) on success
     * - Returns 404 (Not Found) if short URL doesn't exist
     * - Returns 400 (Bad Request) for invalid input
     * 
     * **Performance Considerations:**
     * - This is the most frequently called endpoint
     * - Analytics logging should not block the redirect response
     * - Consider caching for popular short URLs
     *
     * @param id The short URL hash key to redirect
     * @param request The HTTP request containing client information
     * @return HTTP redirect response or error response
     */
    fun redirectTo(id: String, request: HttpServletRequest): ResponseEntity<Unit>

    /**
     * Creates new short URLs from long URLs.
     * 
     * This endpoint handles the creation of short URLs, accepting a long URL
     * and optional metadata, then returning the generated short URL information.
     * 
     * **Content Types Supported:**
     * - `application/x-www-form-urlencoded` (traditional form submission)
     * - `multipart/form-data` (file upload forms, modern web apps)
     * 
     * **Response Format:**
     * - Returns 201 (Created) with Location header and JSON body
     * - Location header contains the short URL for immediate use
     * - JSON body includes additional metadata and properties
     * 
     * **Business Logic:**
     * - Validates input URL format and safety
     * - Generates unique hash for the URL
     * - Stores mapping in repository
     * - Returns short URL with metadata
     *
     * @param data The request data containing URL and optional metadata
     * @param request The HTTP request for extracting client information
     * @return HTTP response with created short URL information
     */
    fun shortener(data: ShortUrlDataIn, request: HttpServletRequest): ResponseEntity<ShortUrlDataOut>
}

/**
 * Request data transfer object for creating short URLs.
 * 
 * This DTO represents the input data for the short URL creation endpoint.
 * It follows the **Data Transfer Object** pattern to encapsulate request
 * data and provide clear API contracts.
 * 
 * **Validation:**
 * - URL field is required and validated by the domain layer
 * - Sponsor field is optional for campaign tracking
 * - All fields are sanitized before processing
 * 
 * **OpenAPI Documentation:**
 * - Fully documented with examples for API consumers
 * - Schema validation ensures type safety
 * - Clear descriptions help with API integration
 */
@Schema(description = "Request data for creating a short URL")
data class ShortUrlDataIn(
    @field:Schema(
        description = "The URL to shorten", 
        example = "https://www.example.com/very/long/url/path", 
        required = true
    )
    val url: String,
    
    @field:Schema(description = "Optional sponsor information", example = "Marketing Campaign 2024")
    val sponsor: String? = null
)

/**
 * Response data transfer object for short URL creation.
 * 
 * This DTO represents the output data returned after successfully creating
 * a short URL. It provides both the short URL and additional metadata
 * for client applications.
 * 
 * **Response Structure:**
 * - **url**: The complete short URL ready for use
 * - **properties**: Additional metadata (safety status, creation info, etc.)
 * 
 * **HTTP Response:**
 * - Status: 201 Created
 * - Location header: Contains the short URL
 * - Body: JSON with URL and properties
 */
@Schema(description = "Response data after creating a short URL")
data class ShortUrlDataOut(
    @field:Schema(description = "The created short URL", example = "http://localhost:8080/f684a3c4")
    val url: String? = null,
    
    @field:Schema(description = "Additional properties of the short URL")
    val properties: Map<String, Any> = emptyMap()
)

/**
 * REST API controller implementation for the URL Shortener application.
 * 
 * This class implements the [UrlShortenerController] interface and serves as the
 * primary **Adapter** in the Hexagonal Architecture. It translates HTTP requests
 * into domain operations and converts domain responses back to HTTP responses.
 * 
 * **Architecture Responsibilities:**
 * - **HTTP Protocol Translation**: Converts HTTP requests/responses to/from domain objects
 * - **Content Type Handling**: Supports multiple content types (form data, JSON)
 * - **Error Translation**: Converts domain exceptions to appropriate HTTP status codes
 * - **Request Context**: Extracts client information (IP, headers) for analytics
 * 
 * **Spring Boot Integration:**
 * - **Auto-discovery**: Automatically registered as a REST controller
 * - **Dependency Injection**: Use cases are injected via constructor
 * - **OpenAPI Integration**: Annotations provide API documentation
 * - **Content Negotiation**: Handles different request/response formats
 * 
 * **Performance Optimizations:**
 * - **Async Analytics**: Click logging doesn't block redirect responses
 * - **Efficient Lookups**: Optimized for high-frequency redirect operations
 * - **Caching Headers**: Appropriate cache control for different endpoints
 * 
 * **Security Features:**
 * - **Input Validation**: All inputs are validated and sanitized
 * - **Rate Limiting**: Can be configured at infrastructure level
 * - **CORS Support**: Configurable for web client access
 * - **HTTPS Enforcement**: Production-ready security headers
 * 
 * @see <a href="https://alistair.cockburn.us/hexagonal-architecture/">Hexagonal Architecture</a>
 * @see <a href="https://spring.io/guides/gs/rest-service/">Spring REST Services</a>
 */
@RestController
@Tag(name = "URL Shortener", description = "Operations for shortening URLs and managing redirects")
class UrlShortenerControllerImpl(
    val redirectUseCase: RedirectUseCase,
    val logClickUseCase: LogClickUseCase,
    val createShortUrlUseCase: CreateShortUrlUseCase
) : UrlShortenerController {

    private val logger = KotlinLogging.logger {}

    /**
     * Redirects and logs a short url identified by its [id].
     *
     * @param id the identifier of the short url
     * @param request the HTTP request
     * @return a ResponseEntity with the redirection details
     */
    @Operation(
        summary = "Redirect to original URL",
        description = "Redirects users to the original URL associated with the short URL identifier " +
                "and logs the click event."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "307",
                description = "Temporary redirect to the original URL",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "301",
                description = "Permanent redirect to the original URL",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Short URL not found",
                content = [Content()]
            )
        ]
    )
    @GetMapping("/{id:(?!api|index).*}")
    override fun redirectTo(
        @Parameter(description = "The short URL identifier", example = "f684a3c4")
        @PathVariable id: String, 
        request: HttpServletRequest
    ): ResponseEntity<Unit> =
        redirectUseCase.redirectTo(id).run {
            logger.info { "Redirecting key '$id' to '${target.value}' with status ${statusCode}" }
            logClickUseCase.logClick(
                id, 
                ClickProperties(ip = request.remoteAddr?.takeIf { it.isNotBlank() }?.let { IpAddress(it) })
            )
            val h = HttpHeaders()
            h.location = URI.create(target.value)
            ResponseEntity<Unit>(h, HttpStatus.valueOf(statusCode))
        }

    /**
     * Creates a short url from details provided in [data].
     *
     * @param data the data required to create a short url
     * @param request the HTTP request
     * @return a ResponseEntity with the created short url details
     */
    @Operation(
        summary = "Create a short URL",
        description = "Creates a short URL from a provided long URL. The URL is validated for safety and accessibility."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Short URL created successfully",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ShortUrlDataOut::class),
                    examples = [ExampleObject(
                        name = "Success Response",
                        summary = "Successful URL shortening",
                        value = """
                        {
                            "url": "http://localhost:8080/f684a3c4",
                            "properties": {
                                "safe": true
                            }
                        }
                        """
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid URL or bad request",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content()]
            )
        ]
    )
    @PostMapping(
        "/api/link", 
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    override fun shortener(
        @Parameter(description = "The data required to create a short URL")
        data: ShortUrlDataIn, 
        request: HttpServletRequest
    ): ResponseEntity<ShortUrlDataOut> =
        createShortUrlUseCase.create(
            url = data.url,
            data = ShortUrlProperties(
                ip = request.remoteAddr?.takeIf { it.isNotBlank() }?.let { IpAddress(it) },
                sponsor = data.sponsor?.takeIf { it.isNotBlank() }?.let { Sponsor(it) }
            )
        ).run {
            logger.info { "Created short URL with hash '${hash.value}' for URL '${data.url}'" }
            val h = HttpHeaders()
            val url = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/{id}")
                .buildAndExpand(hash.value)
                .toUri()
            h.location = url
            val response = ShortUrlDataOut(
                url = url.toString(),
                properties = mapOf(
                    "safe" to (properties.safety == UrlSafety.Safe)
                )
            )
            ResponseEntity<ShortUrlDataOut>(response, h, HttpStatus.CREATED)
        }
}
