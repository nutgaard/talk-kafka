package no.utgdev.plugins.kafka

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class WsServer(
    private val producer: Producer<UUID, String>,
    private val consumer: Consumer<UUID, String>,
    application: ApplicationEnvironment
) {
    private val sessions = mutableListOf<WebSocketServerSession>()
    private val consumerJob = CoroutineScope(Dispatchers.IO).launch {
        consumer.subscribe(listOf(KafkaUtils.wsTopic))
        while (true) {
            val records = consumer.poll(10.seconds.toJavaDuration())
            records.forEach { record ->
                onKafkaData(record.value())
            }
            consumer.commitSync()
        }
    }
    init {
        application.monitor.subscribe(ApplicationStopping) {
            consumer.close()
            consumerJob.cancel()
        }
    }

    fun  onConnect(session: DefaultWebSocketServerSession) {
        sessions.add(session)
        println("Added connection to ws-server")
    }

    fun  onDisconnect(session: DefaultWebSocketServerSession) {
        sessions.remove(session)
        println("Removed connection to ws-server")
    }

    fun  onWsData(data: String) {
        val record = ProducerRecord(KafkaUtils.wsTopic, UUID.randomUUID(), data)
        producer.send(record)
    }

    private suspend fun onKafkaData(data: String) {
        sessions.forEach {
            it.send(Frame.Text(data))
        }
    }
}