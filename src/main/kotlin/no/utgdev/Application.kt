package no.utgdev

import io.ktor.server.engine.*
import io.ktor.server.cio.*
import no.utgdev.plugins.*
import no.utgdev.plugins.kafka.kafkaWs

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureHTTP()
        configureSerialization()
        configureSockets()
        configureAdministration()
        kafkaWs()
    }.start(wait = true)
}
