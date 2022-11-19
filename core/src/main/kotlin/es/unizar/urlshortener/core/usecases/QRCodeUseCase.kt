package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.QRService
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.ShortURLQRCode
import es.unizar.urlshortener.core.ShortUrlRepositoryService

/**
 * Given a URI saves a QR code that links to the resource passed.
 */
interface QRCodeUseCase {
    fun generateQRCode(hash: String) : ShortURLQRCode
}

/**
 * Implementation of [QRCodeUseCase].
 */

class QRCodeUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val qrService: QRService
) : QRCodeUseCase  {
    override fun generateQRCode(hash: String) : ShortURLQRCode {
        val it = shortUrlRepository
            .findByKey(hash)
            ?.redirection
            ?: throw RedirectionNotFound(hash)

        return qrService.generateQRCode(it.target, "$hash-qr.png")
    }
}
