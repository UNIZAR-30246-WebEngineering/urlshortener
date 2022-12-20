@file:Suppress("WildcardImport", "NestedBlockDepth")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.exists

/**
 * Given a URI saves a QR code that links to the resource passed.
 */
interface QRCodeUseCase {
    fun getQR(hash: String) : ShortURLQRCode
}

/**
 * Implementation of [QRCodeUseCase].
 */
class QRCodeUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService
) : QRCodeUseCase  {
    override fun getQR (hash: String): ShortURLQRCode {
        val shortUrl = shortUrlRepository.findByKey(hash)
        shortUrl?.let {
            // Existe la URL
            shortUrl.properties.safe?.let { safe ->
                // Safe no es null miramos si es true o false
                if (!safe) {
                    throw RedirectUnsafeException()
                } else {
                    val path = Paths.get("src/main/resources/static/qr/$hash.png")

                    if (path.exists()) {
                        val file = File("$path")
                        val imageBytes = file.readBytes()
                        return ShortURLQRCode(imageBytes, "$hash.png")
                    } else {
                        throw QrCodeNotFoundException()
                    }
                }
            // No ha sido validada safe es null
            } ?: throw RedirectionNotValidatedException(RETRY_AFTER)
        // No existe la URL es null
        } ?: throw RedirectionNotValidatedException(RETRY_AFTER)
    }
}
