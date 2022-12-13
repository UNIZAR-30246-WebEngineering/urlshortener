package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.WebUnreachable
import org.springframework.http.HttpStatus
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
// import java.time.Duration

// private const val REQUEST_HEAD_TIMEOUT = 2L

interface ReachableWebUseCase {
    fun reachable(url: String)
}

/**
 * Implementation of [ReachableWebUseCase].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class ReachableWebUseCaseImpl : ReachableWebUseCase {
    override fun reachable(url: String) {
        val client = HttpClient.newBuilder()
//            .connectTimeout(Duration.ofSeconds(REQUEST_HEAD_TIMEOUT))
            .build()

        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .method("HEAD", BodyPublishers.noBody())
            .build()

        try {
            val response = client.send(request, BodyHandlers.discarding())

            if (response.statusCode() >= HttpStatus.BAD_REQUEST.value()) {
                throw WebUnreachable(url)
            }
        } catch (cause: Throwable) {
            throw WebUnreachable(url)
        }
    }
}
