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
		val requester = RSocketRequester.builder().tcp("localhost", 8888)
		if (args[0].toString() == "redirect") {
			// Required a Hash
			val result = requester.route("redirect")
				.data(args[1].toString())
				.retrieveMono<String>()
				.onErrorResume{throwable -> Mono.just(throwable.toString())}
				.block()
			println("Got : $result")
		} else if (args[0].toString() == "short") {
			// Required a URI <https://www.google.com
			val result = requester.route("create")
				.data(args[1].toString())
				.retrieveMono<String>()
				.onErrorResume{throwable -> Mono.just(throwable.toString())}
				.block()
			println("Got : $result")
		} else if (args[0].toString() == "qr") {
			// Required a Hash
			val result = requester.route("qr")
				.data(args[1].toString())
				.retrieveMono<String>()
				.onErrorResume{throwable -> Mono.just(throwable.toString())}
				.block()
			println("Got : $result")
		} else {
			println("Opción invaldia, prueba alguna de estas otras:")
			println("redirect <hash>")
			println("short <https://www.google.com>")
			println("qr <hash>")
		}


		Thread.sleep(5000)
	}
}

fun main(args: Array<String>) {
	SpringApplication.run(Application::class.java, *args)
}