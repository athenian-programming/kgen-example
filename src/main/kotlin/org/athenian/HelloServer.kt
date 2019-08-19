package org.athenian

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import java.io.IOException

fun main() {
    HelloServer()
        .apply {
            start()
            server?.awaitTermination()
        }
}

class HelloServer(private val port: Int = 8080) {

    var server: Server? = null

    @Throws(IOException::class)
    fun start() {
        server = ServerBuilder.forPort(port)
            .addService(HelloServiceImpl())
            .addService(ProtoReflectionService.newInstance())
            .build()
            .start()

        println("Server started on port: $port")

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                System.err.println("Shutting down gRPC server...")
                server?.shutdown()
                System.err.println("Server shut down.")
            }
        })
    }
}
