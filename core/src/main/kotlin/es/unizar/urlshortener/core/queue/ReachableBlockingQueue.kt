package es.unizar.urlshortener.core.queue

import es.unizar.urlshortener.core.usecases.ReachableWebUseCase
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.BlockingQueue

@Suppress("TooGenericExceptionCaught", "UnusedPrivateMember", "EmptyTryBlock")
@Component
open class ReachableBlockingQueue(
    private val reachableQueue: BlockingQueue<String>,
    private val reachableWebUseCase: ReachableWebUseCase
) {
    @Async("executorConfig")
    @Scheduled(fixedDelay = 500L)
    open
    fun executor() {
        if (!reachableQueue.isEmpty()) {
            val result = reachableQueue.take()
            reachableWebUseCase.reachable(result)
            println(result)
        }
    }
}
