package es.unizar.urlshortener.infrastructure.delivery

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.hash.Hashing
import es.unizar.urlshortener.core.*
import io.github.g0dkar.qrcode.QRCode
import io.github.g0dkar.qrcode.render.Colors
import net.minidev.json.JSONObject
import org.apache.commons.validator.routines.UrlValidator
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CompletableFuture

const val SUCCESS_STATUS_CODE: Int = 200

/**
 * Implementation of the port [ValidatorService].
 */
class ValidatorServiceImpl : ValidatorService {
    override fun isValid(url: String) = urlValidator.isValid(url)

    override fun isReachable(url: String): Boolean {
        val urlObj = URL(url)
        val huc: HttpURLConnection = urlObj.openConnection() as HttpURLConnection
        huc.instanceFollowRedirects = false

        val responseCode: Int = huc.responseCode

        return responseCode == HttpURLConnection.HTTP_OK
    }

    override fun isSecure(url: String): Boolean {
        // create a JSON object
        val rootObject = JSONObject()
        val subClient = JSONObject()
        subClient["clientId"] = "urlshortener-unizar-gh";
        subClient["clientVersion"] = "1.5.2";
        rootObject["client"] = subClient

        val threatTypes: Array<String> = arrayOf("THREAT_TYPE_UNSPECIFIED")
        val platformTypes: Array<String> = arrayOf("PLATFORM_TYPE_UNSPECIFIED")
        val threatEntryTypes: Array<String> = arrayOf("URL")
        val subClient2 = JSONObject()
        val threatEntry = JSONObject()
        threatEntry["url"] = url
        val threatEntries: Array<JSONObject> = arrayOf(threatEntry)
        subClient2["threatTypes"] = threatTypes
        subClient2["platformTypes"] = platformTypes
        subClient2["threatEntryTypes"] = threatEntryTypes
        subClient2["threatEntries"] = threatEntries
        rootObject["threatInfo"] = subClient2

        val httpClient = HttpClient.newBuilder().build();
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://safebrowsing.googleapis.com/v4/threatMatches:find?key=AIzaSyCWAthHelYHIebU1PATkYxuiEJVK_QRrHk"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(rootObject.toString()))
            .build();

        println(rootObject.toString())
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        println("respuesta:" + response.body().toString())
        println(response.body().toString() == "{}\n")

        if (response.body().toString() == "{}\n") {
            return true
        } else {
            throw UnsafeURIException()
        }
    }

    companion object {
        val urlValidator = UrlValidator(arrayOf("http", "https"))
    }
}

/**
 * Implementation of the port [LocationService].
 */
class LocationServiceImpl : LocationService {
    //override suspend fun getLocation(lat: Double?, lon: Double?, ip: String?): LocationData {
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
     * Example of response from openstreetmap api:
     * https://nominatim.openstreetmap.org/reverse?format=json&lat=41.641412477417894&lon=-0.8800855922769534
     */
    private fun getLocationByCord(lat: Double, lon: Double): LocationData {

        val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}")
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = "GET"

        if (con.responseCode == SUCCESS_STATUS_CODE) {
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
     * Example of response from ip-api.com api:
     * http://ip-api.com/json/yourPublicIP
     */
    private fun getLocationByIp(ip: String): LocationData {
        val url = URL("http://ip-api.com/json/${ip}")
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = "GET"

        if (con.responseCode == SUCCESS_STATUS_CODE) {
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
