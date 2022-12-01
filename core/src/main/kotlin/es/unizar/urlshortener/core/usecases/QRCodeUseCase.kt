package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.QRService
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.ShortURLQRCode
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.exists

/**
 * Given a URI saves a QR code that links to the resource passed.
 */
interface QRCodeUseCase {
    //fun generateQRCode(hash: String) : ShortURLQRCode
    fun getQR(id: String) : ShortURLQRCode
}

/**
 * Implementation of [QRCodeUseCase].
 */

class QRCodeUseCaseImpl(
    //private val shortUrlRepository: ShortUrlRepositoryService,
    private val qrService: QRService
) : QRCodeUseCase  {
    /*override fun generateQRCode(hash: String) : ShortURLQRCode {
        val it = shortUrlRepository
            .findByKey(hash)
            ?.redirection
            ?: throw RedirectionNotFound(hash)

        return qrService.generateQRCode(it.target, "$hash-qr.png")
    }*/

    override fun getQR (id: String): ShortURLQRCode {
        val path = Paths.get("src/main/resources/static/qr/$id.png")
        var imageBytes: ByteArray = ByteArray(0)
        if (path.exists()) {
            val file = File("$path")
            imageBytes = file.readBytes()
        }
        return ShortURLQRCode(imageBytes, "$id.png")
    }
}
