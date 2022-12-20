package es.unizar.urlshortener.core

class InvalidUrlException(val url: String) : Exception("[$url] does not follow a supported schema")

class RedirectionNotFound(val key: String) : Exception("[$key] is not known")

class WebUnreachable(val url: String) : Exception("[$url] is not reachable")

class InfoNotAvailable(val key: String, val msg: String) : Exception("[$key] [$msg] is not available yet")
