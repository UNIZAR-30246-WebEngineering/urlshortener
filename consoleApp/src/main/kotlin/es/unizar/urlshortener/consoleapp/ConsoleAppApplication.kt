package es.unizar.urlshortener.consoleapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ConsoleAppApplication

fun main(args: Array<String>) {
	runApplication<ConsoleAppApplication>(*args)
}
