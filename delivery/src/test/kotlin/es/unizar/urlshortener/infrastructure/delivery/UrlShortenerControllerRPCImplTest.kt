package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.RedirectUseCase
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.util.AssertionErrors
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import org.mockito.BDDMockito.given
import org.springframework.messaging.rsocket.retrieveMono


@WebMvcTest
@ContextConfiguration(
    classes = [
        UrlShortenerControllerRPCImpl::class,
        RestResponseEntityExceptionHandler::class]
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UrlShortenerControllerRPCImplTest {
    @Autowired
    private lateinit var urlShortenerControllerRPCImpl: UrlShortenerControllerRPCImpl

    @MockBean
    private lateinit var redirectUseCase: RedirectUseCase

    @MockBean
    private lateinit var createShortUrlUseCase: CreateShortUrlUseCase

    @MockBean
    private lateinit var requester: RSocketRequester

    // Inicializar rsocket requester

    /*@BeforeAll
    fun setupOnce(@Autowired builder: RSocketRequester, @Value("\${spring.rsocket.server.port}") port: Int?) {
        requester = builder
            .tcp("http://localhost", 8888)
    }*/

    @AfterAll
    fun tearDownOnce() {
        requester!!.rsocket()!!.dispose()
    }

    @Test
    fun `getRedirectUseCase returns a redirect when the key exist`() {
        /*val result: Mono<String> = requester
            .route("create")
            .data("http://example.com/ qr 0")
            .retrieveMono(String::class.java)*/

        /*
        StepVerifier
            .create(result)
            .consumeNextWith { message ->
                println(message.toString())
                AssertionErrors.assertEquals("El mensaje", message.toString(), "http://localhost:8080/f684a3c4")
            }
            .verifyComplete()
         */

        given(
            requester
                .route("create")
                .data("http://example.com/ qr 0")
                .retrieveMono(String::class.java)
        ).willReturn(Mono.just("http://localhost:8080/f684a3c4"))


    }
/*
    @Test
    fun `getLogClickUseCase returns a hash when the key exist`() {
        val hash = urlShortenerControllerRPCImpl.shortener("http://example.com/ qr 0")

        AssertionErrors.assertEquals("Equals:", hash, "http://localhost:8080/f684a3c4")
    }

    @Test
    fun `getQrCodeUseCase returns a the link to the qr is exists`() {
        //Create the hash and qr
        urlShortenerControllerRPCImpl.shortener("http://example.com/ qr 0").let {
            val response = urlShortenerControllerRPCImpl.qr(it)
            AssertionErrors.assertEquals("Equals:", response, "http://localhost:8080/f684a3c4/qr")
        }
    }

    @Test
    fun `getQrCodeUseCase returns an error is the qr doesnt exist`() {
        val response = urlShortenerControllerRPCImpl.qr("invent")
        AssertionErrors.assertEquals("Equals:", response, "No existe ningún qr con ese hash: invent")
    }*/
}