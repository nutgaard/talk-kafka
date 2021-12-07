package no.utgdev.plugins.kafka

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*

fun Application.kafkaWs() {
    val kafkaBroker = "localhost:9092"
    val producer = KafkaUtils.createProducer("ws-producer", kafkaBroker)
    val consumer = KafkaUtils.createConsumer("ws-consumer", "ws-consumer", kafkaBroker)
    val wsServer = WsServer(producer, consumer, environment)

    routing {
        get("/kafka") {
            call.respond("All set and done")
        }

        webSocket("/kafka/ws") { // websocketSession
            try {
                wsServer.onConnect(this)
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> wsServer.onWsData(frame.readText())
                        else -> println("Received unknown type: ${frame.javaClass}")
                    }
                }
            } finally {
                wsServer.onDisconnect(this)
            }
        }
    }
}