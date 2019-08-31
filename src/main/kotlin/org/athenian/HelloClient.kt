package org.athenian

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random
import kotlin.system.exitProcess

fun main() {
    try {
        val channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build()
        val client = HelloServiceClient.create(channel)

        syncClient(client)
        streamingClient(client)
        streamingServer(client)
        bidirectionalService(client)

        client.shutdownChannel()

        exitProcess(0)
    } catch (t: Throwable) {
        System.err.println("Failed: $t")
    }
    exitProcess(1)
}

fun syncClient(client: HelloServiceClient) =
    runBlocking {
        val request = hiRequest {
            query = "Hello!"
            tags = listOf("greeting", "salutation")
            flags = mapOf("hello" to "hi", "later" to "bye")
        }

        val response = client.hiThere(request)
        println("Sync response was: ${response.result}")
    }

fun streamingClient(client: HelloServiceClient) {
    val call = client.hiThereWithManyRequests()

    runBlocking {
        launch {
            repeat(5) {
                val request = hiRequest { query = "Hello Again! $it" }
                call.requests.send(request)
            }
            call.requests.close()
        }

        val response = call.response.await()
        println("Streaming Client result = ${response.result}")
    }
}

fun streamingServer(client: HelloServiceClient) {
    val request = hiRequest { query = "Bill" }
    val replies = client.hiThereWithManyResponses(request).responses

    runBlocking {
        println("Streaming Server results:")
        for (reply in replies)
            println(reply.result)
        println()
    }
}

fun bidirectionalService(client: HelloServiceClient) {
    val call = client.hiThereWithManyRequestsAndManyResponses()

    runBlocking {

        launch {
            repeat(5) {
                val s = "Mary $it"
                val request = hiRequest { query = s }
                call.requests.send(request)
                println("Async client sent $s")
                delay(Random.nextLong(1_000))
            }
            call.requests.close()
        }

        launch {
            for (response in call.responses) {
                println("Async response from server = ${response.result}")
                delay(Random.nextLong(1_000))
            }
        }
    }
}
