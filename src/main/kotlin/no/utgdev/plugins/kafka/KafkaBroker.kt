package no.utgdev.plugins.kafka

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import no.utgdev.plugins.kafka.KafkaConcept.roundRobinOf
import java.lang.Long.min

object KafkaConcept {
    fun loadTopics(): List<Topic> = emptyList()
    fun <T> roundRobinOf(group: List<T>): T = group.first()

    class Consumer(val consumerId: String) {
        val subscriptions: MutableSet<Topic> = mutableSetOf()

        fun subscribe(topic: Topic) {
            subscriptions.add(topic)
        }

        fun unsubscribe(topic: Topic) {
            subscriptions.remove(topic)
        }
    }

    class Topic(val name: String, val partitions: List<Partition>) {
    }
    class Partition() {
        fun getOffsetForCurrentConsumer(): Long = 0
        fun getLastOffset(): Long = 0
        fun read(count: Long): List<Record> = emptyList()
        fun write(records: List<Record>): Unit {}
    }
    class Record

    class Subscription(val consumerId: String, val topicNames: List<String>)
    class Unsubscription(val consumerId: String, val topicNames: List<String>)
    class Poll(
        val consumerId: String,
        val maxPollRecords: Long
    )
    class Publish(
        val topic: String,
        val records: List<Record>
    )
}

@ExperimentalCoroutinesApi
fun Application.kafkaBroker() {
    val topics: List<KafkaConcept.Topic> = KafkaConcept.loadTopics()
    val consumers: MutableList<KafkaConcept.Consumer> = mutableListOf()

    routing {
        post("/subscribe") {
            val request: KafkaConcept.Subscription = context.receive()
            val consumer = consumers.find { it.consumerId == request.consumerId }
                ?: KafkaConcept.Consumer(request.consumerId)

            request.topicNames
                .mapNotNull { topic -> topics.find { it.name == topic } }
                .forEach { topic ->
                    consumer.subscribe(topic)
                }
        }
        post("/unsubscribe") {
            val request: KafkaConcept.Unsubscription = context.receive()
            val consumer = consumers.find { it.consumerId == request.consumerId }
                ?: throw IllegalStateException("Could not find consumer")

            request.topicNames.forEach { topic ->
                consumer.subscriptions.removeIf { it.name == topic }
            }

            if (consumer.subscriptions.isEmpty()) {
                consumers.remove(consumer)
            }
        }

        post("/poll") {
            val request: KafkaConcept.Poll = context.receive()
            val consumer = consumers.find { it.consumerId == request.consumerId }
                ?: throw IllegalStateException("Could not find consumer")

            val partitions = consumer.subscriptions.flatMap {
                it.partitions
            }
            val maxRecords = partitions
                .sumOf { it.getLastOffset() - it.getOffsetForCurrentConsumer() }

            var outRecords = min(maxRecords, request.maxPollRecords)
            val data = partitions.map {
                val rest = it.getLastOffset() - it.getOffsetForCurrentConsumer()
                val consumeCount = min(rest, outRecords)
                outRecords -= consumeCount
                async {
                    it.read(consumeCount)
                }
            }

            val result = ArrayList<KafkaConcept.Record>(outRecords.toInt())

            data.awaitAll()
            data.forEach {
                result.addAll(it.getCompleted())
            }

            context.respond(result)
        }

        post("publish") {
            val request: KafkaConcept.Publish = context.receive()
            val topic = topics.find { it.name == request.topic }
                ?: throw IllegalStateException("Could not find topic")

            request.records
                .groupBy { roundRobinOf(topic.partitions) }
                .forEach { (partition, records) ->
                    partition.write(records)
                }
        }
    }
}