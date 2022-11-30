package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import io.github.g0dkar.qrcode.QRCode
import java.io.ByteArrayOutputStream

interface QrCodeUseCase {
    fun generateQR(id: String, url: String): ByteArray
}

/**
 * Implementation of [QrCodeUseCase].
 */
class QrCodeUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService
) : QrCodeUseCase {
    override fun generateQR(id: String, url: String): ByteArray {
        shortUrlRepository
            .findByKey(id)
            ?.redirection
            ?: throw RedirectionNotFound(id)

        val imageOut = ByteArrayOutputStream()

        QRCode(url).render().writeImage(imageOut)

        return imageOut.toByteArray()
    }
}
