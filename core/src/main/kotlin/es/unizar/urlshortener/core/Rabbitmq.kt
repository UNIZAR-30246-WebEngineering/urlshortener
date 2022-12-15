package es.unizar.urlshortener.core


interface Rabbitmq {
    fun proveUrl(message: String)

}
