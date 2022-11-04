package es.unizar.urlshortener.core.usecases

import com.fasterxml.jackson.databind.ObjectMapper;
import es.unizar.urlshortener.core.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Given an url returns the key that is used to create a short URL.
 * When the url is created optional data may be added.
 *
 * **Note**: This is an example of functionality.
 */
interface CreateShortUrlUseCase {
    fun create(url: String, data: ShortUrlProperties): ShortUrl
}

/**
 * Implementation of [CreateShortUrlUseCase].
 */
class CreateShortUrlUseCaseImpl(
        private val shortUrlRepository: ShortUrlRepositoryService,
        private val validatorService: ValidatorService,
        private val hashService: HashService
) : CreateShortUrlUseCase {
    override fun create(url: String, data: ShortUrlProperties): ShortUrl =
        if (validatorService.isValid(url) && validatorService.isReachable(url)) {
            // Get the location from the coordinates or the ip
            val location: LocationData = getLocation(data.lat, data.lon, data.ip)

            val id: String = hashService.hasUrl(url)
            val su = ShortUrl(
                hash = id,
                redirection = Redirection(target = url),
                properties = ShortUrlProperties(
                    safe = data.safe,
                    ip = data.ip,
                    sponsor = data.sponsor,
                    lat = location.lat,
                    lon = location.lon,
                    country = location.country,
                    city = location.city,
                    state = location.state,
                    road = location.road,
                    cp = location.cp,
                )
            )
            shortUrlRepository.save(su)
        } else {
            throw InvalidUrlException(url)
        }

    private fun getLocation(lat: Double?, lon: Double?, ip: String?): LocationData {
        if (lat != null && lon != null) {
            // Get the location from the coordinates
            return getLocationByCord(lat, lon)
        } else if (ip != null) {
            // Get the location from the ip
            return getLocationByIp(ip)
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
        var location = LocationData()

        val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}")
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = "GET"

        if (con.responseCode == 200) {
            val mapper = ObjectMapper()
            // Parsear el json devuelto
            val json = mapper.readTree(con.inputStream)
            print(lat.toString() + " " + lon.toString())

            location = LocationData(
                    lat = lat,
                    lon = lon,
                    country = json.get("address").get("country").asText(),
                    city = json.get("address").get("city").asText(),
                    state = json.get("address").get("state").asText(),
                    road = json.get("address").get("road").asText(),
                    cp = json.get("address").get("postcode").asText()
            )
        } else {
            // Raise an exception if the response code from the API is not 200
            throw InvalidLocationException()
        }
        con.disconnect()
        return location
    }

    /**
     * Get location from ip.
     * Example of response from ip-api.com api:
     * http://ip-api.com/json/yourPublicIP
     */
     private fun getLocationByIp(ip: String): LocationData {
        var location = LocationData()
        val url = URL("http://ip-api.com/json/${ip}")
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = "GET"

        if (con.responseCode == 200) {
            val mapper = ObjectMapper()
            // Parsear el json devuelto
            val json = mapper.readTree(con.inputStream)

            if (json.get("status").asText() == "success") {
                location = LocationData(
                        lat = json.get("lat").asDouble(),
                        lon = json.get("lon").asDouble(),
                        country = json.get("country").asText(),
                        city = json.get("city").asText(),
                        state = json.get("regionName").asText(),
                        road = json.get("isp").asText(),
                        cp = json.get("zip").asText()
                )
            } else {
                // Raise an exception if the ip is not valid cause the ip is not public
                throw InvalidLocationException()
            }
        } else {
            // Raise an exception if the response code from the API is not 200
            throw InvalidLocationException()
        }
        con.disconnect()
        return location
     }
}
