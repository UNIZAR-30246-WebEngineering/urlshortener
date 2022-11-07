package es.unizar.urlshortener.infrastructure.delivery

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.hash.Hashing
import es.unizar.urlshortener.core.ValidatorService
import es.unizar.urlshortener.core.LocationService
import es.unizar.urlshortener.core.LocationData
import es.unizar.urlshortener.core.InvalidLocationException
import es.unizar.urlshortener.core.HashService
import org.apache.commons.validator.routines.UrlValidator
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

const val SUCCESS_STATUS_CODE: Int = 200

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
 * Implementation of the port [LocationService].
 */
class LocationServiceImpl : LocationService {
    override fun getLocation(lat: Double?, lon: Double?, ip: String?): LocationData {
        return if (lat != null && lon != null) {
            // Get the location from the coordinates
            getLocationByCord(lat, lon)
        } else if (ip != null) {
            // Get the location from the ip
            getLocationByIp(ip)
        } else {
            // Throw an exception if the coordinates and the ip are null
            throw InvalidLocationException()
        }
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

            val location = LocationData(
                    lat = lat,
                    lon = lon,
                    country = json.get("address").get("country").asText(),
                    city = json.get("address").get("city").asText(),
                    state = json.get("address").get("state").asText(),
                    road = json.get("address").get("road").asText(),
                    cp = json.get("address").get("postcode").asText()
            )
            con.disconnect()
            return location
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
