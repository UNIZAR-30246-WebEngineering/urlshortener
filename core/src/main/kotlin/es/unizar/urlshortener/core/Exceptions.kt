package es.unizar.urlshortener.core

class InvalidUrlException(val url: String) : Exception("[$url] does not follow a supported schema")

class RedirectionNotFound(val key: String) : Exception("[$key] is not known")

class InvalidLocationException() : Exception("Could not get a valid location")
