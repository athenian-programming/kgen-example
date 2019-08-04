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

fun syncClient(client: HelloServiceClient) {

    runBlocking {
        val syncResponse =
            client.hiThere(
                hiRequest {
                    query = "Hello!"
                    tags = listOf("greeting", "salutation")
                    flags = mapOf("hello" to "hi", "later" to "bye")
                })
        println("Sync response was: ${syncResponse.result}")
    }
}

fun streamingClient(client: HelloServiceClient) {
    runBlocking {
        val streamingCall = client.hiThereWithManyRequests()

        launch {
            repeat(5) {
                streamingCall.requests.send(hiRequest { query = "Hello Again! $it" })
            }
            streamingCall.requests.close()
        }

        val result = streamingCall.response.await()
        println("Streaming Client result = ${result.result}")
    }
}

fun streamingServer(client: HelloServiceClient) {
    runBlocking {
        val streamingServerCall = client.hiThereWithManyResponses(hiRequest { query = "Bill" })
        for (resp in streamingServerCall.responses)
            println("Streaming Server result = ${resp.result}")
    }
}

fun bidirectionalService(client: HelloServiceClient) {
    runBlocking {

        val bidirectionalCall = client.hiThereWithManyRequestsAndManyResponses()

        launch {
            repeat(5) {
                val s = "Mary $it"
                bidirectionalCall.requests
                    .send(
                        hiRequest {
                            query = s
                        }
                    )
                println("Async client sent $s")
                delay(Random.nextLong(1000))
            }
            bidirectionalCall.requests.close()
        }

        launch {
            for (resp in bidirectionalCall.responses) {
                println("Async response from server = ${resp.result}")
                delay(Random.nextLong(1000))
            }
        }
    }
}
