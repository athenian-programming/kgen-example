package org.athenian

import io.grpc.ManagedChannelBuilder
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

    fun runExample() = runBlocking {
        val client =
            HelloServiceClient.create(
                channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build()
            )

        val result1 =
            client.hiThere(
                hiRequest {
                    query = "Hello!"
                    tags = listOf("greeting", "salutation")
                    flags = mapOf(
                        "hello" to "hi",
                        "later" to "bye"
                    )
                })

        println("The response was: ${result1.result}")


        val call1 = client.hiThereWithManyRequests()
        repeat(3) {
            call1.requests.send(
                hiRequest {
                    query = "Hello Again!"
                })
        }
        call1.requests.close()
        val result2 = call1.response.await()
        println("Result2 = ${result2.result}")

        val call2 = client.hiThereWithManyReponses(hiRequest { query = "Bill" })
        for (resp in call2.responses) {
            println("Result3 = ${resp.result}")
        }

        val call3 = client.hiThereWithManyRequestsAndManyReponses()
        repeat(3) {
            call3.requests.send(
                hiRequest {
                    query = "Hello Again!"
                })
        }
        call3.requests.close()
        for (resp in call3.responses) {
            println("Result4 = ${resp.result}")
        }


        client.shutdownChannel()
    }
}
