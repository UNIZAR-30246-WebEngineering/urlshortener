@file:Suppress("WildcardImport")

package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.LogClickUseCase
import es.unizar.urlshortener.core.usecases.RedirectUseCase
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.Test

@WebMvcTest
@ContextConfiguration(
    classes = [
        UrlShortenerControllerImpl::class,
        ExceptionHandlers::class
    ]
)
class UrlShortenerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var redirectUseCase: RedirectUseCase

    @MockitoBean
    private lateinit var logClickUseCase: LogClickUseCase

    @MockitoBean
    private lateinit var createShortUrlUseCase: CreateShortUrlUseCase

    /**
     * Tests that `redirectTo` returns a redirect when the key exists.
     */
    @Test
    fun `redirectTo returns a redirect when the key exists`() {
        // Mock the behavior of redirectUseCase to return a redirection URL
        given(redirectUseCase.redirectTo("key")).willReturn(Redirection(Url("http://example.com/")))

        // Perform a GET request and verify the response status and redirection URL
        mockMvc.perform(get("/{id}", "key"))
            .andExpect(status().isTemporaryRedirect)
            .andExpect(redirectedUrl("http://example.com/"))

        // Verify that logClickUseCase logs the click with the correct IP address
        verify(logClickUseCase).logClick("key", ClickProperties(ip = IpAddress("127.0.0.1")))
    }

    /**
     * Tests that `redirectTo` returns a not found status when the key does not exist.
     */
    @Test
    fun `redirectTo returns a not found when the key does not exist`() {
        // Mock the behavior of redirectUseCase to throw a RedirectionNotFound exception
        given(redirectUseCase.redirectTo("key"))
            .willAnswer { throw RedirectionNotFound("key") }

        // Perform a GET request and verify the response status and Problem Details format
        mockMvc.perform(get("/{id}", "key"))
            .andDo(print())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Redirection Not Found"))
            .andExpect(jsonPath("$.detail").exists())

        // Verify that logClickUseCase does not log the click
        verify(logClickUseCase, never()).logClick("key", ClickProperties(ip = IpAddress("127.0.0.1")))
    }

    /**
     * Tests that `creates` returns a basic redirect if it can compute a hash.
     */
    @Test
    fun `creates returns a basic redirect if it can compute a hash`() {
        // Mock the behavior of createShortUrlUseCase to return a ShortUrl object
        given(
            createShortUrlUseCase.create(
                url = "http://example.com/",
                data = ShortUrlProperties(ip = IpAddress("127.0.0.1"))
            )
        ).willReturn(ShortUrl(UrlHash("f684a3c4"), Redirection(Url("http://example.com/"))))

        // Perform a POST request and verify the response status, redirection URL, and JSON response
        mockMvc.perform(
            post("/api/link")
                .param("url", "http://example.com/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(redirectedUrl("http://localhost/f684a3c4"))
            .andExpect(jsonPath("$.url").value("http://localhost/f684a3c4"))
    }

    /**
     * Tests that `creates` returns a bad request status if it cannot compute a hash.
     */
    @Test
    fun `creates returns bad request if it can compute a hash`() {
        // Mock the behavior of createShortUrlUseCase to throw an InvalidUrlException
        given(
            createShortUrlUseCase.create(
                url = "ftp://example.com/",
                data = ShortUrlProperties(ip = IpAddress("127.0.0.1"))
            )
        ).willAnswer { throw InvalidUrlException("ftp://example.com/") }

        // Perform a POST request and verify the response status and Problem Details format
        mockMvc.perform(
            post("/api/link")
                .param("url", "ftp://example.com/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Invalid URL"))
            .andExpect(jsonPath("$.detail").exists())
    }

    /**
     * Tests that `creates` returns a bad request status for invalid input (empty URL).
     */
    @Test
    fun `creates returns bad request for empty URL`() {
        // Mock the behavior of createShortUrlUseCase to throw an InvalidInputException
        given(
            createShortUrlUseCase.create(
                url = "",
                data = ShortUrlProperties(ip = IpAddress("127.0.0.1"))
            )
        ).willAnswer { throw InvalidInputException("url", "") }

        // Perform a POST request and verify the response status and error message
        mockMvc.perform(
            post("/api/link")
                .param("url", "")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Invalid Input"))
            .andExpect(jsonPath("$.detail").exists())
    }

    /**
     * Tests that `creates` returns a bad request status for invalid input (blank URL).
     */
    @Test
    fun `creates returns bad request for blank URL`() {
        // Mock the behavior of createShortUrlUseCase to throw an InvalidInputException
        given(
            createShortUrlUseCase.create(
                url = "   ",
                data = ShortUrlProperties(ip = IpAddress("127.0.0.1"))
            )
        ).willAnswer { throw InvalidInputException("url", "   ") }

        // Perform a POST request and verify the response status and error message
        mockMvc.perform(
            post("/api/link")
                .param("url", "   ")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Invalid Input"))
            .andExpect(jsonPath("$.detail").exists())
    }

    /**
     * Tests that `creates` returns a bad request status for URL with invalid characters.
     */
    @Test
    fun `creates returns bad request for URL with invalid characters`() {
        // Mock the behavior of createShortUrlUseCase to throw an InvalidUrlException
        given(
            createShortUrlUseCase.create(
                url = "http://example.com/\u0000",
                data = ShortUrlProperties(ip = IpAddress("127.0.0.1"))
            )
        ).willAnswer { throw InvalidUrlException("http://example.com/\u0000") }

        // Perform a POST request and verify the response status and error message
        mockMvc.perform(
            post("/api/link")
                .param("url", "http://example.com/\u0000")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Invalid URL"))
            .andExpect(jsonPath("$.detail").exists())
    }

    /**
     * Tests that `creates` returns a bad request status for URL with URL-encoded invalid characters.
     */
    @Test
    fun `creates returns bad request for URL with URL-encoded invalid characters`() {
        // Mock the behavior of createShortUrlUseCase to throw an InvalidUrlException
        given(
            createShortUrlUseCase.create(
                url = "http://example.com/%20invalid%00path",
                data = ShortUrlProperties(ip = IpAddress("127.0.0.1"))
            )
        ).willAnswer { throw InvalidUrlException("http://example.com/%20invalid%00path") }

        // Perform a POST request and verify the response status and error message
        mockMvc.perform(
            post("/api/link")
                .param("url", "http://example.com/%20invalid%00path")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Invalid URL"))
            .andExpect(jsonPath("$.detail").exists())
    }

    /**
     * Tests that `redirectTo` returns a bad request status for invalid input (blank key).
     */
    @Test
    fun `redirectTo returns bad request for blank key`() {
        // Mock the behavior of redirectUseCase to throw an InvalidInputException
        given(redirectUseCase.redirectTo("   "))
            .willAnswer { throw InvalidInputException("key", "   ") }

        // Perform a GET request and verify the response status and error message
        mockMvc.perform(get("/{id}", "   "))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Invalid Input"))
            .andExpect(jsonPath("$.detail").exists())
    }

    /**
     * Tests that `shortener` returns a basic redirect using multipart form data.
     */
    @Test
    fun `creates returns a basic redirect using multipart form data`() {
        // Mock the behavior of createShortUrlUseCase to return a short URL
        given(
            createShortUrlUseCase.create(
                url = "http://example.com/",
                data = ShortUrlProperties(ip = IpAddress("127.0.0.1"))
            )
        ).willReturn(ShortUrl(UrlHash("f684a3c4"), Redirection(Url("http://example.com/"))))

        // Perform a POST request and verify the response status, redirection URL, and JSON response
        mockMvc.perform(
            post("/api/link")
                .param("url", "http://example.com/")
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(redirectedUrl("http://localhost/f684a3c4"))
            .andExpect(jsonPath("$.url").value("http://localhost/f684a3c4"))
    }
}
