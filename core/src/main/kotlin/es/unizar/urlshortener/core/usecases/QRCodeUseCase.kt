package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.ShortURLQRCode
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.exists

/**
 * Given a URI saves a QR code that links to the resource passed.
 */
interface QRCodeUseCase {
    fun getQR(id: String) : ShortURLQRCode
}

/**
 * Implementation of [QRCodeUseCase].
 */
class QRCodeUseCaseImpl : QRCodeUseCase  {
    override fun getQR (id: String): ShortURLQRCode {
        val path = Paths.get("src/main/resources/static/qr/$id.png")
        var imageBytes = ByteArray(0)
        if (path.exists()) {
            val file = File("$path")
            imageBytes = file.readBytes()
        }
        return ShortURLQRCode(imageBytes, "$id.png")
    }
}
