package es.unizar.urlshortener.core.queue

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.BlockingQueue

@Suppress("TooGenericExceptionCaught", "UnusedPrivateMember", "EmptyTryBlock")
@Component
open class QRBlockingQueue {

    @Qualifier("qrqueue")
    @Autowired
    private val qrQueue: BlockingQueue<String>? = null

    @Async("executorConfig")
    @Scheduled(fixedDelay = 200L)
    open
    fun executor() {
        try {
        } catch (e: Exception) {
            println(e.toString())
        }
    }
}
