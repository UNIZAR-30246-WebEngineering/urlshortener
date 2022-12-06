package bootiful.rpc.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.RSocketRequester;
import java.util.Locale;

fun main(args: Array<String>) {
	System.setProperty("spring.profiles.active", "rpcclient")
	runApplication<BootifulApplication>(*args)
}

@SpringBootApplication
class BootifulApplication {
	@Bean
	fun rSocketRequester(builder: RSocketRequester.Builder): RSocketRequester {
		return builder.tcp("localhost", 8888)
	}

	@Bean
	fun ready(rSocketRequester: RSocketRequester): ApplicationListener<ApplicationReadyEvent> {
		return ApplicationListener<ApplicationReadyEvent> { event: ApplicationReadyEvent? ->
			rSocketRequester //
				.route("greetings.{lang}", Locale.ENGLISH) //
				.data("World").retrieveMono<String>(String::class.java) //
				.subscribe { greetings -> println("got: $greetings") }
		}
	}
}