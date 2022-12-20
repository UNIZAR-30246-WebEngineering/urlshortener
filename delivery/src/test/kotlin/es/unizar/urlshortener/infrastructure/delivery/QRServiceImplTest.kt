package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.LocationData
import es.unizar.urlshortener.core.ShortURLQRCode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.util.AssertionErrors.assertEquals
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture

@AutoConfigureMockMvc
class QRServiceImplTest {
    @Autowired
    private lateinit var qrServiceImpl: QRServiceImpl

    @BeforeEach
    fun setUp() {
        qrServiceImpl = QRServiceImpl()
    }

    @Test
    fun `generateQRCode returns a CompletableFuture`() {
        val shortURLQRCode = qrServiceImpl.generateQRCode(
            uri = "https://www.google.com",
            filename = "test.png"
        )
        assertEquals("QR generated:", CompletableFuture<ShortURLQRCode>().javaClass , shortURLQRCode.javaClass)
    }
}
