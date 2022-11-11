package es.unizar.urlshortener.infrastructure.delivery

import com.google.common.hash.Hashing
import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.ValidatorService
import org.apache.commons.validator.routines.UrlValidator
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

/**
 * Implementation of the port [ValidatorService].
 */
class ValidatorServiceImpl : ValidatorService {
    override fun isValid(url: String) = urlValidator.isValid(url) and googleSafeBrowsing(url)

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

/**
 * ###Implementation of the functionality of Google Safe Browsing
 * Return true if the response is empty *(" {}")* that means that the url is secure, false otherwise
 */
fun googleSafeBrowsing(url: String) : Boolean {
    val valuesGoogle = "{\"client\":{\"clientId\":\"accessSafe\",\"clientVersion\":\"1.0\"},\"threatInfo\"" +
            ":{\"threatTypes\":[\"MALWARE\",\"SOCIAL_ENGINEERING\", \"UNWANTED_SOFTWARE\", \"THREAT_TYPE_UNSPECIFIED\", " +
            "\"POTENTIALLY_HARMFUL_APPLICATION\"], \"platformTypes\":[\"WINDOWS\"],\"threatEntryTypes\":[\"URL\"]," +
            "\"threatEntries\":[{\"url\":\"" + url + "\"}]}}"

    // Código inspirado en https://zetcode.com/kotlin/getpostrequest/
    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://safebrowsing.googleapis.com/v4/threatMatches:find?key=AIzaSyA-X7PVj9e2vuaxT5nqyp58TcFa4gBQNo4"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(valuesGoogle))
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    return response.body().length == 3
}