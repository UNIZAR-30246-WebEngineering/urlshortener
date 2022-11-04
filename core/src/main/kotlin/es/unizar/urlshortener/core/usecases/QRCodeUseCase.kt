package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.InvalidUrlException
import es.unizar.urlshortener.core.ValidatorService
import io.github.g0dkar.qrcode.QRCode
import io.github.g0dkar.qrcode.render.Colors
import java.io.FileOutputStream

/**
 * Given a URI saves a QR code that links to the resource passed.
 */
interface QRCodeUseCase {
    fun generateQRCode(content: String, filename: String)
}

/**
 * Implementation of [QRCodeUseCase].
 */
/*
class QRCodeUseCaseImpl(
    private val validatorService: ValidatorService,
) : CreateShortUrlUseCase  {
    fun generateQRCode(uri: String, filename: String) {
        if (validatorService.isValid(uri)) {
            //  squareColor     =>  darkColor
            //  backgroundColor =>  brightColor
            val qrCodeCanvas = QRCode(uri).render(darkColor = Colors.css("#0D1117"),
                    brightColor = Colors.css("#8B949E"))
            val fileOut = FileOutputStream("$filename.png")
            qrCodeCanvas.writeImage(fileOut)
        } else {
            throw InvalidUrlException(uri)
        }
    }
}*/
