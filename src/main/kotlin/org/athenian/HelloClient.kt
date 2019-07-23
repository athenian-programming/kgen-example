package org.athenian

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class HelloClient {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                HelloClient().runExample()
                System.exit(0)
            } catch (t: Throwable) {
                System.err.println("Failed: $t")
            }
            System.exit(1)
        }
    }

    fun runExample() =
        runBlocking {
            val client =
                HelloServiceClient.create(
                    channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build()
                )

            val syncResponse =
                client.hiThere(
                    hiRequest {
                        query = "Hello!"
                        tags = listOf("greeting", "salutation")
                        flags = mapOf(
                            "hello" to "hi",
                            "later" to "bye"
                        )
                    })

            println("Sync response was: ${syncResponse.result}")


            val streamingClientCall = client.hiThereWithManyRequests()
            repeat(3) {
                streamingClientCall.requests.send(
                    hiRequest {
                        query = "Hello Again! $it"
                    })
            }
            streamingClientCall.requests.close()
            val streamingClientResult = streamingClientCall.response.await()
            println("Streaming Client result = ${streamingClientResult.result}")

            val streamingServerCall = client.hiThereWithManyReponses(hiRequest { query = "Bill" })
            for (resp in streamingServerCall.responses)
                println("Streaming Server result = ${resp.result}")

            val bidirectionalCall = client.hiThereWithManyRequestsAndManyReponses()

            val sender = async {
                repeat(5) {
                    bidirectionalCall.requests.send(
                        hiRequest {
                            query = "Hello val $it"
                        }
                    )
                    println("Sent val in async")
                    delay(1_000)
                }
                bidirectionalCall.requests.close()
            }

            val receiver = async {
                bidirectionalCall
                    .responses
                    .consumeEach {
                        println("Async response from server = ${it.result}")
                        delay(1_000)
                    }
            }

            sender.join()
            receiver.join()

            client.shutdownChannel()
        }
}
