package es.unizar.urlshortener.links.adapters.hashing

import com.google.common.hash.Hashing
import es.unizar.urlshortener.links.application.HashGenerator
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class MurmurHashGenerator : HashGenerator {
    override fun hash(url: String): String = Hashing.murmur3_32_fixed().hashString(url, StandardCharsets.UTF_8).toString()
}
