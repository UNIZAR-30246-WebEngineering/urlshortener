package es.unizar.urlshortener.infrastructure.delivery

import com.google.common.hash.Hashing
import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.QrService
import es.unizar.urlshortener.core.ValidatorService
import io.github.g0dkar.qrcode.QRCode
import org.apache.commons.validator.routines.UrlValidator
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

/**
 * Implementation of the port [ValidatorService].
 */
class ValidatorServiceImpl : ValidatorService {
    override fun isValid(url: String) = urlValidator.isValid(url)

    companion object {
        val urlValidator = UrlValidator(arrayOf("http", "https"))
    }
}

/**
 * Implementation of the port [HashService].
 */
@Suppress("UnstableApiUsage")
class HashServiceImpl : HashService {
    override fun hasUrl(url: String) = Hashing.murmur3_32_fixed().hashString(url, StandardCharsets.UTF_8).toString()
}

class QrServiceImpl : QrService {
    override fun getQr(url: String): ByteArrayResource =
        ByteArrayOutputStream().let {
            QRCode(url).render().writeImage(it)
            ByteArrayResource(it.toByteArray(), IMAGE_PNG_VALUE)
        }
}
