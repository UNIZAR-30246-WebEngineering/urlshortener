@file:Suppress("NoWildcardImports", "WildcardImport", "SpreadOperator")
package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.QRCodeUseCase
import es.unizar.urlshortener.core.usecases.RedirectUseCase
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest
@ContextConfiguration(
    classes = [
        UrlShortenerControllerImpl::class,
        RestResponseEntityExceptionHandler::class]
)
class UrlShortenerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var redirectUseCase: RedirectUseCase

    @MockBean
    private lateinit var logClickUseCase: LogClickUseCase

    @MockBean
    private lateinit var createShortUrlUseCase: CreateShortUrlUseCase

    @MockBean
    private lateinit var qrCodeUseCase: QRCodeUseCase

    @Test
    fun `redirectTo returns a redirect when the key exists`() {
        given(redirectUseCase.redirectTo("key")).willReturn(Redirection("http://example.com/"))

        mockMvc.perform(get("/{id}", "key"))
            .andExpect(status().isTemporaryRedirect)
            .andExpect(redirectedUrl("http://example.com/"))

        verify(logClickUseCase).logClick("key", ClickProperties(ip = "127.0.0.1"))
    }

    @Test
    fun `redirectTo returns a not found when the key does not exist`() {
        given(redirectUseCase.redirectTo("key"))
            .willAnswer { throw RedirectionNotFound("key") }

        mockMvc.perform(get("/{id}", "key"))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.statusCode").value(404))

        verify(logClickUseCase, never()).logClick("key", ClickProperties(ip = "127.0.0.1"))
    }

    @Test
    fun `creates returns a basic redirect if it can compute a hash`() {
        given(
            createShortUrlUseCase.create(
                url = "http://www.example.com/",
                data = ShortUrlProperties(
                    ip = "127.0.0.1",
                    lat = 42.223,
                    lon = 1.223,
                    qr = false
                )
            )
        ).willReturn(ShortUrl("f684a3c4", Redirection("http://www.example.com/")))

        mockMvc.perform(
            post("/api/link")
                .param("url", "http://www.example.com/")
                .param("lat", "42.223")
                .param("lon", "1.223")
                .param("qr", "false")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
        .andDo(print())
        .andExpect(status().isCreated)
        .andExpect(redirectedUrl("http://localhost/f684a3c4"))
        .andExpect(jsonPath("$.url").value("http://localhost/f684a3c4"))
    }

    @Test
    fun `creates returns bad request if it can compute a hash`() {
        given(
            createShortUrlUseCase.create(
                url = "ftp://example.com/",
                data = ShortUrlProperties(
                    ip = "127.0.0.1",
                    qr = false
                )
            )
        ).willAnswer { throw InvalidUrlException("ftp://example.com/") }

        mockMvc.perform(
            post("/api/link")
                .param("url", "ftp://example.com/")
                .param("qr", "false")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.statusCode").value(400))
    }

    @Test
    fun `redirectTo returns too many requests when limit is reached`() {

        given(
            createShortUrlUseCase.create(
                url = "http://www.example.com/",
                data = ShortUrlProperties(
                    ip = "127.0.0.1",
                    lat = 42.223,
                    lon = 1.223,
                    limit = 1,
                    qr = false
                )
            )
        ).willReturn(ShortUrl("f684a3c4", Redirection("http://www.example.com/")))

        given(
            redirectUseCase.redirectTo("f684a3c4")
        ).willReturn(Redirection("http://www.example.com/")
        ).willAnswer { throw TooManyRedirectionsException("key", 100L) }

        mockMvc.perform(
            post("/api/link")
                .param("url", "http://www.example.com/")
                .param("lat", "42.223")
                .param("lon", "1.223")
                .param("limit", "1")
                .param("qr", "false")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
        .andDo(print())
        .andExpect(status().isCreated)
        .andExpect(redirectedUrl("http://localhost/f684a3c4"))
        .andExpect(jsonPath("$.url").value("http://localhost/f684a3c4"))

        mockMvc.perform(get("/{id}", "f684a3c4"))
            .andExpect(status().isTemporaryRedirect)
            .andExpect(redirectedUrl("http://www.example.com/"))

        mockMvc.perform(get("/{id}", "f684a3c4"))
            .andExpect(status().isTooManyRequests)
            .andExpect(header().exists("Retry-After"))
    }

    @Test
    fun `getQr throws a bad request if the uri not secure`() {
        val unsafeUri = "https://www.faturamaga.com/"
        val unsafeHash = "ea9a3b86"

        given(
            createShortUrlUseCase.create(
                url = unsafeUri,
                data = ShortUrlProperties(
                    ip = "127.0.0.1",
                    lat = 42.223,
                    lon = 1.223,
                    qr = true
                )
            )
        ).willReturn(ShortUrl(unsafeHash, Redirection(unsafeUri)))

        given(
            qrCodeUseCase.getQR(
                hash = unsafeHash
            )
        ).willAnswer { throw RedirectUnsafeException() }

        mockMvc.perform(
            post("/api/link")
                .param("url", unsafeUri)
                .param("lat", "42.223")
                .param("lon", "1.223")
                .param("qr", "true")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(redirectedUrl("http://localhost/$unsafeHash"))
            .andExpect(jsonPath("$.url").value("http://localhost/$unsafeHash"))

        mockMvc.perform(
            get("http://localhost/{hash}/qr", unsafeHash)
        )
            .andDo(print())
            .andExpect(status().isForbidden)
    }

    @Test
    fun `getQr throws forbidden if the uri is not safe`() {
        given(
            qrCodeUseCase.getQR(
                hash = "qwerty"
            )
        ).willAnswer { throw RedirectionNotValidatedException() }

        mockMvc.perform(
            get("http://localhost/{hash}/qr", "qwerty")
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `getQr returns a valid QR if the hash exists` () {
        given(
            createShortUrlUseCase.create(
                url = "http://www.example.com/",
                data = ShortUrlProperties(
                    ip = "127.0.0.1",
                    lat = 42.223,
                    lon = 1.223,
                    qr = true
                )
            )
        ).willReturn(ShortUrl("f684a3c4", Redirection("http://www.example.com/")))

        given(
            qrCodeUseCase.getQR(
                hash = "f684a3c4"
            )
        ).willReturn(ShortURLQRCode(ByteArray(0), "f684a3c4.png"))

        mockMvc.perform(
            post("/api/link")
                .param("url", "http://www.example.com/")
                .param("lat", "42.223")
                .param("lon", "1.223")
                .param("qr", "true")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(redirectedUrl("http://localhost/f684a3c4"))
            .andExpect(jsonPath("$.url").value("http://localhost/f684a3c4"))

        mockMvc.perform(
            get("http://localhost/{hash}/qr", "f684a3c4")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
    }

    @Test
    fun `getQr returns bad request if the qr can't be found` () {
        given(
            qrCodeUseCase.getQR(
                hash = "f684a3c4"
            )
        ).willAnswer { throw QrCodeNotFoundException() }

        mockMvc.perform(
            get("http://localhost/{hash}/qr", "f684a3c4")
        )
        .andDo(print())
        .andExpect(status().isBadRequest)
    }
}
