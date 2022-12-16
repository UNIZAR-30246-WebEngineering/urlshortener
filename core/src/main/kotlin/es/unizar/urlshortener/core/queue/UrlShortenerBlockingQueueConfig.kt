package es.unizar.urlshortener.core.queue

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue

private const val POOL_SIZE = 20
private const val CORE_POOL_SIZE = 20
private const val QUEUE_CAPACITY = 1000

@Configuration
@EnableAsync
@EnableScheduling
open class UrlShortenerBlockingQueueConfig {

    @Bean("qrqueue")
    open fun qrQueue(): BlockingQueue<String> = LinkedBlockingQueue()

    @Bean("reachablequeue")
    open fun reachableQueue(): BlockingQueue<String> = LinkedBlockingQueue()

    @Bean("executorConfig")
    open fun executor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.maxPoolSize = POOL_SIZE
        executor.corePoolSize = CORE_POOL_SIZE
        executor.setQueueCapacity(QUEUE_CAPACITY)
        executor.initialize()
        return executor
    }
}
