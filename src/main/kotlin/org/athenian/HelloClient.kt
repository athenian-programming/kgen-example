package org.athenian

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random
import kotlin.system.exitProcess

fun main() {
    try {
        val client =
            HelloServiceClient.create(
                channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build()
            )

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
        val request =
            hiRequest {
                query = "Hello!"
                tags = listOf("greeting", "salutation")
                flags = mapOf("hello" to "hi", "later" to "bye")
            }

        val response = client.hiThere(request)
        println("Sync response was: ${response.result}")
    }

fun streamingClient(client: HelloServiceClient) =
    runBlocking {
        val clientStreamingCall = client.hiThereWithManyRequests()

        launch {
            repeat(5) {
                val request = hiRequest { query = "Hello Again! $it" }
                clientStreamingCall.requests.send(request)
            }
            clientStreamingCall.requests.close()
        }

        val response = clientStreamingCall.response.await()
        println("Streaming Client result = ${response.result}")
    }

fun streamingServer(client: HelloServiceClient) =
    runBlocking {
        val request = hiRequest { query = "Bill" }
        val streamingServerCall = client.hiThereWithManyResponses(request)
        for (response in streamingServerCall.responses)
            println("Streaming Server result = ${response.result}")
    }

fun bidirectionalService(client: HelloServiceClient) =
    runBlocking {
        val streamingCall = client.hiThereWithManyRequestsAndManyResponses()

        launch {
            repeat(5) {
                val s = "Mary $it"
                val request = hiRequest { query = s }
                streamingCall.requests.send(request)
                println("Async client sent $s")
                delay(Random.nextLong(1_000))
            }
            streamingCall.requests.close()
        }

        launch {
            for (response in streamingCall.responses) {
                println("Async response from server = ${response.result}")
                delay(Random.nextLong(1_000))
            }
        }
    }
