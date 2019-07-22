package org.athenian

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import java.io.IOException

class HelloServer(private val port: Int = 8080) {
    companion object {
        @JvmStatic
        @Throws(IOException::class, InterruptedException::class)
        fun main(args: Array<String>) {
            HelloServer().apply {
                start()
                server?.awaitTermination()
            }
        }
    }

    private var server: Server? = null

    @Throws(IOException::class)
    private fun start() {
        server = ServerBuilder.forPort(port)
            .addService(HelloServiceImpl())
            .addService(ProtoReflectionService.newInstance())
            .build()
            .start()

        println("Server started on port: $port")

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                System.err.println("Shutting down gRPC server...")
                this@HelloServer.server?.shutdown()
                System.err.println("Server shut down.")
            }
        })
    }
}
