package org.athenian

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking

class Client {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            try {
                Client().runExample()
                System.exit(0)
            } catch (t: Throwable) {
                System.err.println("Failed: $t")
            }
            System.exit(1)
        }
    }

    fun runExample() = runBlocking {
        // create a client with an insecure channel
        val client = HelloServiceClient.create(
            channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build()
        )

        // call the API
        val result = client.hiThere(hiRequest {
            // set a normal field
            query = "Hello!"

            // set a repeated field
            tags = listOf("greeting", "salutation")

            // set a map field
            flags = mapOf(
                "hello" to "hi",
                "later" to "bye"
            )
        })

        // print the result
        println("The response was: ${result.result}")

        // shutdown
        client.shutdownChannel()
    }
}
