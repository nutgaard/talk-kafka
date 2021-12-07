package no.utgdev.plugins.kafka

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.*
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

object KafkaUtils {
    const val wsTopic = "wstopic"

    fun createProducer(clientId: String, bootstrapServers: String): Producer<UUID, String> {
        val properties = Properties().apply {
            put(ProducerConfig.ACKS_CONFIG, "all")
            put(ProducerConfig.CLIENT_ID_CONFIG, clientId)
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer::class.java)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
        }
        return KafkaProducer(properties)
    }

    fun createConsumer(clientId: String, groupId: String, bootstrapServers: String): Consumer<UUID, String> {
        val properties = Properties().apply {
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false)
            put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000)
            put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 10.minutes.toInt(DurationUnit.MILLISECONDS))
            put(ConsumerConfig.CLIENT_ID_CONFIG, clientId)
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, UUIDDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        }
        return KafkaConsumer(properties)
    }
}