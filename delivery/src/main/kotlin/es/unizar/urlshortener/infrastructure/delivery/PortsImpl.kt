@file:Suppress("WildcardImport")
package es.unizar.urlshortener.infrastructure.delivery

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.hash.Hashing
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import es.unizar.urlshortener.core.*
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCaseImpl
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import io.github.g0dkar.qrcode.QRCode
import io.github.g0dkar.qrcode.render.Colors
import net.minidev.json.JSONObject
import org.apache.commons.validator.routines.UrlValidator
import org.springframework.http.HttpStatus
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

const val REFILL_RATE = 60L

/**
 * Implementation of the port [ValidatorService].
 */
class ValidatorServiceImpl : ValidatorService {
    override fun isValid(url: String) = urlValidator.isValid(url)

    /**
     * Checks if the URL is reachable.
     * @param url URL to check.
     * @return true if the URL is reachable, false otherwise.
     */
    override fun isReachable(url: String): Boolean {
        val urlObj = URL(url)
        val huc: HttpURLConnection = urlObj.openConnection() as HttpURLConnection
        huc.instanceFollowRedirects = false

        val responseCode: Int = huc.responseCode

        return responseCode == HttpURLConnection.HTTP_OK
    }

    /**
     * Checks if the URL is secure.
     *
     * @param url the URL to check.
     * @return true if the URL is secure, false otherwise.
     * @throws UnsafeURIException if the URL is not secure.
     * Example of unsafe URI:
     * https://www.zipl.in/construction/slider/up/
     */
    override fun isSecure(url: String): Boolean {
        val threatTypes: Array<String> = arrayOf("THREAT_TYPE_UNSPECIFIED", "MALWARE","SOCIAL_ENGINEERING","UNWANTED_SOFTWARE","MALICIOUS_BINARY","POTENTIALLY_HARMFUL_APPLICATION")
        val platformTypes: Array<String> = arrayOf("ALL_PLATFORMS")
        val threatEntryTypes: Array<String> = arrayOf("URL")

        // create a JSON object
        val json = JSONObject()
        val client = JSONObject()
        val threatInfo = JSONObject()
        val threatEntry = JSONObject()

        client["clientId"] = "urlshortener-unizar-gh"
        client["clientVersion"] = "1.5.2"
        json["client"] = client

        threatEntry["url"] = url
        val threatEntries: Array<JSONObject> = arrayOf(threatEntry)
        threatInfo["threatTypes"] = threatTypes
        threatInfo["platformTypes"] = platformTypes
        threatInfo["threatEntryTypes"] = threatEntryTypes
        threatInfo["threatEntries"] = threatEntries
        json["threatInfo"] = threatInfo

        val httpClient = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://safebrowsing.googleapis.com/v4/threatMatches:find?key=AIzaSyCWAthHelYHIebU1PATkYxuiEJVK_QRrHk"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body().toString() == "{}\n"
    }

    companion object {
        val urlValidator = UrlValidator(arrayOf("http", "https"))
    }
}

/**
 * Implementation of the port [LocationService].
 */
class LocationServiceImpl : LocationService {

    /**
     * Gets the location of the URL.
     * @param lat latitude of the location.
     * @param lon longitude of the location.
     * @param ip IP of the user.
     * @return the location of the URL.
     * @throws InvalidLocationException if the location of the user is not found.
     */
    override fun getLocation(lat: Double?, lon: Double?, ip: String?): CompletableFuture<LocationData> {
        val location = if (lat != null && lon != null) {
            // Get the location from the coordinates
            getLocationByCord(lat, lon)
        } else if (ip != null) {
            // Get the location from the ip
            getLocationByIp(ip)
        } else {
            // Throw an exception if the coordinates and the ip are null
            throw InvalidLocationException()
        }
        return CompletableFuture.completedFuture(location)
    }

    /**
     * Get location from lat and lon.
     * @param lat latitude.
     * @param lon longitude.
     * @return location data.
     * @throws InvalidLocationException if the location is not valid.
     * Example of response from openstreetmap api:
     * https://nominatim.openstreetmap.org/reverse?format=json&lat=41.641412477417894&lon=-0.8800855922769534
     */
     private fun getLocationByCord(lat: Double, lon: Double): LocationData {
        val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}")
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = "GET"

        if (con.responseCode == HttpStatus.OK.value()) {
            val mapper = ObjectMapper()
            // Parsear el json devuelto
            val json = mapper.readTree(con.inputStream)

            if (json.get("error") == null) { // Las coordenadas son válidas
                val location = LocationData(
                        lat = lat,
                        lon = lon,
                        country = json?.get("address")?.get("country")?.asText(),
                        city = json?.get("address")?.get("city")?.asText(),
                        state = json?.get("address")?.get("state")?.asText(),
                        road = json?.get("address")?.get("road")?.asText(),
                        cp = json?.get("address")?.get("postcode")?.asText()
                )
                con.disconnect()
                return location
            } else {
                con.disconnect()
                throw InvalidLocationException()
            }
        } else {
            // Raise an exception if the response code from the API is not 200
            con.disconnect()
            throw InvalidLocationException()
        }
    }

    /**
     * Get location from ip.
     * @param ip ip.
     * @return location data.
     * @throws InvalidLocationException if the location is not valid.
     * Example of response from ip-api.com api:
     * http://ip-api.com/json/yourPublicIP
     */
    private fun getLocationByIp(ip: String): LocationData {
        val url = URL("http://ip-api.com/json/${ip}")
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = "GET"

        if (con.responseCode == HttpStatus.OK.value()) {
            val mapper = ObjectMapper()
            // Parsear el json devuelto
            val json = mapper.readTree(con.inputStream)

            if (json.get("status").asText() == "success") {
                val location = LocationData(
                        lat = json.get("lat").asDouble(),
                        lon = json.get("lon").asDouble(),
                        country = json.get("country").asText(),
                        city = json.get("city").asText(),
                        state = json.get("regionName").asText(),
                        road = json.get("isp").asText(),
                        cp = json.get("zip").asText()
                )
                con.disconnect()
                return location
            } else {
                // Raise an exception if the ip is not valid cause the ip is not public
                con.disconnect()
                throw InvalidLocationException()
            }
        } else {
            // Raise an exception if the response code from the API is not 200
            con.disconnect()
            throw InvalidLocationException()
        }
    }
}

/**
 * Implementation of the port [HashService].
 */
@Suppress("UnstableApiUsage")
class HashServiceImpl : HashService {
    override fun hasUrl(url: String) = Hashing.murmur3_32_fixed().hashString(url, StandardCharsets.UTF_8).toString()
}

class QRServiceImpl : QRService {
    override fun generateQRCode(uri: String, filename: String): ShortURLQRCode {
        val imageOut = ByteArrayOutputStream()

        QRCode(uri).render(
            darkColor = Colors.css("#0D1117"),
            brightColor = Colors.css("#8B949E")
        ).writeImage(imageOut)

        val imageBytes = imageOut.toByteArray()

        return ShortURLQRCode(imageBytes, filename)
    }
}
class RedirectionLimitServiceImpl : RedirectionLimitService {

    private val buckets : ConcurrentHashMap<String, Bucket> = ConcurrentHashMap()
    override fun addLimit(hash: String, limit: Int) {
        println("Creating bucket with limit $limit for URL with hash $hash")
        val bLim = Bandwidth.classic(limit.toLong(), Refill.intervally(limit.toLong(), Duration.ofMinutes(REFILL_RATE)))
        buckets[hash] = Bucket.builder()
            .addLimit(bLim)
            .build()
    }
    override fun checkLimit(hash: String) {
        val bucket = buckets[hash]
        if (bucket != null) {
            val probe = bucket.tryConsumeAndReturnRemaining(1)
            if ( !probe.isConsumed ) {
                throw TooManyRedirectionsException(hash)
            }
        }
    }
}

class RabbitMQServiceImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val validator: ValidatorServiceImpl
) : RabbitMQService {
    private val QUEUE_NAME = "hello"
    private val factory = ConnectionFactory()
    private val connection = factory.newConnection()

    fun RabbitMQServiceImpl() {
        factory.setHost("localhost")
    }

    override fun read() {
        val channel = connection.createChannel()
        println("reading: ..........")
        val deliverCallback = DeliverCallback { consumerTag: String?, delivery: Delivery ->
            val message = String(delivery.body, StandardCharsets.UTF_8)
            println(" [x] Received '$message'")
            val (hash, url) = message.split(" ")
            shortUrlRepository.updateSafe(hash, validator.isSecure(url))
        }
        channel.basicConsume(QUEUE_NAME, true, deliverCallback) { consumerTag: String? -> }
    }
    override fun write(message:String) {
        val channel = connection.createChannel()
        channel.queueDeclare(QUEUE_NAME, false, false, false, null)
        channel.basicPublish("", QUEUE_NAME, null, message.toByteArray())
        System.out.println(" [x] Sent '" + message + "'")
    }
}

