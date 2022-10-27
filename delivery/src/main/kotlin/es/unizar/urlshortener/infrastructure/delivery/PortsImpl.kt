package es.unizar.urlshortener.infrastructure.delivery

import com.google.common.hash.Hashing
import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.ValidatorService
import org.apache.commons.validator.routines.UrlValidator
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Implementation of the port [ValidatorService].
 */
class ValidatorServiceImpl : ValidatorService {
    override fun isValid(url: String) = urlValidator.isValid(url)

    override fun isReachable(url: String): Boolean {
        val url = URL("http://www.example.com")
        val huc: HttpURLConnection = url.openConnection() as HttpURLConnection
        huc.setInstanceFollowRedirects(false)

        val responseCode: Int = huc.getResponseCode()

        return responseCode.equals(HttpURLConnection.HTTP_OK)
    }

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
