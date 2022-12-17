package es.unizar.urlshortener.infrastructure.delivery

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.util.AssertionErrors

@AutoConfigureMockMvc
class ValidatorServiceImplTest {
    @Autowired
    private lateinit var validatorService: ValidatorServiceImpl

    @BeforeEach
    fun setUp() {
        validatorService = ValidatorServiceImpl()
    }

    @Test
    fun `isReachable returns false if the URI is not reachable`() {
        val url = "https://www.google.co"
        val reachable = validatorService.isReachable(url = url)
        AssertionErrors.assertEquals("Uri is not reachable:", false, reachable)
    }

    @Test
    fun `isReachable returns true if the URI is reachable`() {
        val url = "https://www.google.com"
        val reachable = validatorService.isReachable(url = url)
        AssertionErrors.assertEquals("Uri is reachable:", true, reachable)
    }

    @Test
    fun `isSecure returns false if the URI is not secure`() {
        val url = "https://testsafebrowsing.appspot.com/s/phishing.html"
        val secure = validatorService.isSecure(url = url)
        AssertionErrors.assertEquals("Uri is not secure:", false, secure)
    }

    @Test
    fun `isSecure returns true if the URI is secure`() {
        val url = "https://www.google.com"
        val secure = validatorService.isSecure(url = url)
        AssertionErrors.assertEquals("Uri is secure:", true, secure)
    }
}
