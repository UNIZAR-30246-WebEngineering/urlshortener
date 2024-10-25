package es.unizar.urlshortener.gateway

import com.google.gson.annotations.SerializedName
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import es.unizar.urlshortener.core.GeolocationData
import es.unizar.urlshortener.core.usecases.GeolocationUseCase
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class GeolocationUseCaseImpl : GeolocationUseCase {

    private val apiKey = "e459e0bccea04b4e84441f7ece2ae5b5"
    private val client = OkHttpClient()

    override fun getGeolocation(ip: String): GeolocationData? {
        return try {
            val request = Request.Builder()
                .url("https://api.ipgeolocation.io/ipgeo?apiKey=$apiKey&ip=$ip")
                .build()

            client.newCall(request).execute().use { response ->
                handleResponse(response)
            }
        } catch (e: IOException) {
            throw GeolocationServiceException("Error de red en la llamada a la API de geolocalización", e)
        } catch (e: JsonSyntaxException) {
            throw GeolocationServiceException("Error al procesar la respuesta JSON", e)
        }
    }

    /**
     * Procesa la respuesta HTTP y parsea el JSON.
     */
    private fun handleResponse(response: Response): GeolocationData? {
        if (!response.isSuccessful) {
            throw GeolocationServiceException("Error en la llamada a la API de geolocalización: ${response.message}")
        }

        val responseBody = response.body?.string() ?: return null

        val geolocationResponse = Gson().fromJson(responseBody, GeolocationApiResponse::class.java)

        return GeolocationData(
            country = geolocationResponse.countryName ?: "Unknown",
            city = geolocationResponse.city ?: "Unknown",
            region = geolocationResponse.stateProv ?: "Unknown"
        )
    }
}

/**
 * Clase interna para mapear la respuesta de la API.
 */
data class GeolocationApiResponse(
    @SerializedName("country_name") val countryName: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("state_prov") val stateProv: String? = null
)

class GeolocationServiceException(message: String, cause: Throwable? = null) : Exception(message, cause)
