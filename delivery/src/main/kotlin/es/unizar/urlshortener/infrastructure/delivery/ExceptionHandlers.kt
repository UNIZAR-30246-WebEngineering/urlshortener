@file:Suppress("MaxLineLength")
package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.InvalidUrlException
import es.unizar.urlshortener.core.InvalidInputException
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.InternalError
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI
import java.time.OffsetDateTime

/**
 * Global exception handler that implements Problem Details for HTTP APIs (RFC 9457).
 * 
 * This handler provides standardized error responses that include:
 * - **type**: A URI reference that identifies the problem type
 * - **title**: A short, human-readable summary of the problem type
 * - **status**: The HTTP status code
 * - **detail**: A human-readable explanation specific to this occurrence
 * - **instance**: A URI reference that identifies the specific occurrence
 * 
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9457">RFC 9457 - Problem Details for HTTP APIs</a>
 */
@ControllerAdvice
class ExceptionHandlers : ResponseEntityExceptionHandler() {

    private val log = KotlinLogging.logger {}

    /**
     * Handles InvalidUrlException and returns a BAD_REQUEST response with Problem Details.
     *
     * @param ex the InvalidUrlException thrown
     * @param request the WebRequest during which the exception was thrown
     * @return a ProblemDetail following RFC 9457 format
     */
    @ExceptionHandler(value = [InvalidUrlException::class])
    fun invalidUrls(ex: InvalidUrlException, request: WebRequest): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid URL format")
        problemDetail.type = URI.create("https://urlshortener.example.com/problems/invalid-url")
        problemDetail.title = "Invalid URL"
        problemDetail.instance = URI.create(request.getDescription(false))
        problemDetail.setProperty("timestamp", OffsetDateTime.now())
        return problemDetail
    }

    /**
     * Handles InvalidInputException and returns a BAD_REQUEST response with Problem Details.
     *
     * @param ex the InvalidInputException thrown
     * @param request the WebRequest during which the exception was thrown
     * @return a ProblemDetail following RFC 9457 format
     */
    @ExceptionHandler(value = [InvalidInputException::class])
    fun invalidInput(ex: InvalidInputException, request: WebRequest): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid input provided")
        problemDetail.type = URI.create("https://urlshortener.example.com/problems/invalid-input")
        problemDetail.title = "Invalid Input"
        problemDetail.instance = URI.create(request.getDescription(false))
        problemDetail.setProperty("timestamp", OffsetDateTime.now())
        return problemDetail
    }

    /**
     * Handles RedirectionNotFound exception and returns a NOT_FOUND response with Problem Details.
     *
     * @param ex the RedirectionNotFound exception thrown
     * @param request the WebRequest during which the exception was thrown
     * @return a ProblemDetail following RFC 9457 format
     */
    @ExceptionHandler(value = [RedirectionNotFound::class])
    fun redirectionNotFound(ex: RedirectionNotFound, request: WebRequest): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Short URL not found")
        problemDetail.type = URI.create("https://urlshortener.example.com/problems/redirection-not-found")
        problemDetail.title = "Redirection Not Found"
        problemDetail.instance = URI.create(request.getDescription(false))
        problemDetail.setProperty("timestamp", OffsetDateTime.now())
        return problemDetail
    }

    /**
     * Handles InternalError and returns an INTERNAL_SERVER_ERROR response with Problem Details.
     *
     * @param ex the InternalError thrown
     * @param request the WebRequest during which the exception was thrown
     * @return a ProblemDetail following RFC 9457 format
     */
    @ExceptionHandler(value = [InternalError::class])
    fun internalError(ex: InternalError, request: WebRequest): ProblemDetail {
        log.error(ex) { "Internal error: ${ex.message}, Request Details: ${request.getDescription(false)}" }
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred")
        problemDetail.type = URI.create("https://urlshortener.example.com/problems/internal-error")
        problemDetail.title = "Internal Server Error"
        problemDetail.instance = URI.create(request.getDescription(false))
        problemDetail.setProperty("timestamp", OffsetDateTime.now())
        problemDetail.setProperty("errorId", generateErrorId())
        return problemDetail
    }

    /**
     * Generates a unique error ID for tracking purposes.
     * In a production environment, this would be a proper UUID or correlation ID.
     */
    private fun generateErrorId(): String = "ERR-${System.currentTimeMillis()}"
}

