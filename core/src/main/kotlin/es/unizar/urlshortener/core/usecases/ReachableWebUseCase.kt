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
class ReachableWebUseCaseImpl : ReachableWebUseCase {
    override fun reachable(url: String) {
        val client = HttpClient.newBuilder()
//            .connectTimeout(Duration.ofSeconds(REQUEST_HEAD_TIMEOUT))
            .build()

        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .method("HEAD", BodyPublishers.noBody())
            .build()

        val result = client.send(request, BodyHandlers.discarding())

        takeIf { result.statusCode().equals(HttpStatus.OK.value()) } ?: throw WebUnreachable(url)
    }
}
