package es.unizar.urlshortener.consoleapp

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.retrieveMono
import reactor.core.publisher.Mono

@SpringBootApplication
class Application : CommandLineRunner {
	private val log = LoggerFactory.getLogger(Application::class.java)

	@Bean
	fun rSocketRequester(builder: RSocketRequester.Builder): RSocketRequester? {
		return builder.tcp("localhost", 8888)
	}

	override fun run(vararg args: String?) {
		log.info("run")
		var sb = StringBuilder()
		for (option in args) {
			sb.append(" ").append(option)
			log.info("OPTION: {}", option)
		}
		sb = if (sb.length == 0) sb.append("No Options Specified") else sb
		log.info(String.format("WAR launched with following options: %s", sb.toString()))

		val requester = RSocketRequester.builder().tcp("localhost", 8888)

		val result = requester.route("create")
			.data("https://www.google.com")
			.retrieveMono<String>()
			.onErrorResume{throwable -> Mono.just(throwable.toString())}
			.block()


		println("Got : $result")
		Thread.sleep(5000)
	}
}

fun main(args: Array<String>) {
	SpringApplication.run(Application::class.java, *args)
}