package es.unizar.urlshortener.rabbitqueue

import es.unizar.urlshortener.core.ShortUrlRepositoryService
import es.unizar.urlshortener.core.ValidatorService
import org.springframework.stereotype.Component

/**
 * The controller that handles the RabbitMQ messages.
 */
@Component
class Receiver (
    private val shortUrlRepositoryService: ShortUrlRepositoryService,
    private val validatorService: ValidatorService
) {

    /**
     * This method is called automatically when a message is received from the queue.
     * Wh
     * @param message The message received from the queue.
     */
    fun receiveMessage(message: String) {
        println("Received <$message>")
        val url = message.split(" ")[0]
        val hash = message.split(" ")[1]
        shortUrlRepositoryService.updateSafe(hash, validatorService.isSecure(url))
    }
}
