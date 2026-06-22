package es.unizar.urlshortener.core

import java.lang.RuntimeException

/**
 * A base class for domain-specific exceptions in the application.
 * This sealed class serves as a root for all exceptions related to the domain logic.
 * It extends [RuntimeException], allowing for an optional [cause].
 *
 * @param message The detail message for the exception.
 * @param cause The cause of the exception, which can be null.
 */
sealed class DomainException(message: String, cause: Throwable? = null):
    RuntimeException(message, cause)

/**
 * An exception indicating that a provided URL does not follow a supported schema.
 * This exception is thrown when the format or schema of a URL does not match the expected pattern.
 *
 * @param url The URL that caused the exception.
 */
class InvalidUrlException(url: String) : DomainException("[$url] does not follow a supported schema")

/**
 * An exception indicating that the provided input is invalid (null, empty, or too long).
 *
 * @param field The field name that caused the exception.
 * @param value The value that caused the exception.
 */
class InvalidInputException(field: String, value: String?) : 
    DomainException("Invalid input for field '$field': ${value ?: "null"}")

/**
 * An exception indicating that a redirection key could not be found.
 * This exception is thrown when a specified redirection key does not exist in the system.
 *
 * @param key The redirection key that was not found.
 */
class RedirectionNotFound(key: String) : DomainException("[$key] is not known")

/**
 * An exception indicating an internal error within the application.
 * This exception can be used to represent unexpected issues that occur within the application,
 * providing both a message and a cause for the error.
 *
 * @param message The detail message for the exception.
 * @param cause The cause of the exception.
 */
class InternalError(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Sanitizes and validates basic input constraints (null, empty, length).
 * This is for basic input sanitization, not business logic validation.
 *
 * @param input The input string to validate.
 * @param fieldName The name of the field for error reporting.
 * @param maxLength The maximum allowed length (default: 2048).
 * @return The sanitized input string.
 * @throws InvalidInputException if the input is invalid.
 */
@Suppress("ThrowsCount")
fun sanitizeInput(input: String?, fieldName: String, maxLength: Int = 2048): String {
    return when {
        input == null -> throw InvalidInputException(fieldName, null)
        input.isBlank() -> throw InvalidInputException(fieldName, input)
        input.length > maxLength -> throw InvalidInputException(
            fieldName, 
            "length ${input.length} exceeds maximum $maxLength"
        )
        else -> input.trim()
    }
}

inline fun <T> safeCall(
    onFailure: (Throwable) -> Throwable = { e -> InternalError("Unexpected error", e) },
    block: () -> T
): T = runCatching {
    block()
}.fold(
    onSuccess = { it },
    onFailure = { throw onFailure(it) }
)
