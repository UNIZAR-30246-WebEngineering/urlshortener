package es.unizar.urlshortener.core

class InvalidUrlException(val url: String) : Exception("[$url] does not follow a supported schema")

class RedirectionNotFound(val key: String) : Exception("[$key] is not known")

class InvalidLocationException : Exception("Could not get a valid location")

class TooManyRedirectionsException(private val key: String, refillTime: Long) : Exception("Too many redirections for URL $key") {
    val refillTime : Long = refillTime
}

class UnsafeURIException : Exception("URI is not safe")
