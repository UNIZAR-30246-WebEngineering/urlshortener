package es.unizar.urlshortener.core

class InvalidUrlException(val url: String) : Exception("[$url] does not follow a supported schema")

class RedirectionNotFound(val key: String) : Exception("[$key] is not known")

class InvalidLocationException : Exception("Could not get a valid location")

class TooManyRedirectionsException(private val key: String, refillTime: Long) : Exception("Too many redirections for URL $key") {
    val refillTime : Long = refillTime
}

class UnsafeURIException(val url: String) : Exception("[$url] is not safe")

class RedirectUnsafeException : Exception("URI is not safe")

class RedirectionNotValidatedException : Exception("URI has not been validated yet")

class QrCodeNotFoundException : Exception("QR code has not been generated yet")
