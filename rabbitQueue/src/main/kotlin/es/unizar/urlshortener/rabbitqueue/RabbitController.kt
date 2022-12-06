package es.unizar.urlshortener.rabbitqueue

import es.unizar.urlshortener.core.ShortUrlRepositoryService
import org.springframework.stereotype.Component

@Component
class Receiver (
    val shortUrlRepositoryService: ShortUrlRepositoryService
        ) {
    fun receiveMessage(message: String) {
        val hash = message.split(" ")[1]
        println(shortUrlRepositoryService.findByKey(hash))
        println("Received <$message>")
    }
}