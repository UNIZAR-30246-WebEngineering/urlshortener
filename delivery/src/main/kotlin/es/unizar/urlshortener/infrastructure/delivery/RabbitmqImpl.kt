package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.Rabbitmq
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import org.springframework.stereotype.Component

/**
 * Component Rabbitmq
 */
@Component
class RabbitmqImpl(private val shortUrl: ShortUrlRepositoryService) :
    Rabbitmq {

    /**
     * Call the function [googleSafeBrowsing] to prove if the url is secure
     * If is secure call **updateSecuritySecure** to change the parameter safe to true
     * Else call **changeSecurityGoogle** to change the parameter safe to false
     */
    override fun proveUrl(message: String) {
        println("Received < $message >")
        val url = message.substringBefore('-')
        val id = message.substringAfter('-')

        if (!googleSafeBrowsing(url)) {
            shortUrl.changeSecurityGoogle(id)
        } else {
            shortUrl.updateSecuritySecure(id)
        }
    }
}
