package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.QrService
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.ShortUrlRepositoryService
import org.springframework.core.io.ByteArrayResource

interface QrCodeUseCase {
    fun generateQR(id: String, url: String): ByteArrayResource
}

/**
 * Implementation of [QrCodeUseCase].
 */
class QrCodeUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val qrService: QrService
) : QrCodeUseCase {
    override fun generateQR(id: String, url: String): ByteArrayResource =
        shortUrlRepository.findByKey(id)?.let {
            qrService.getQr(url)
        } ?: throw RedirectionNotFound(id)
}
