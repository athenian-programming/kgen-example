package org.athenian

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.stub.StreamObserver
import java.io.IOException

/**
 * Example server.
 *
 * Adapted from the gRPC examples:
 * https://github.com/grpc/grpc-java/blob/master/examples/example-kotlin
 */
class ExampleServer(private val port: Int = 8080) {

    companion object {
        /** Main launches the server from the command line. */
        @JvmStatic
        @Throws(IOException::class, InterruptedException::class)
        fun main(args: Array<String>) {
            val server = ExampleServer()
            server.start()
            server.blockUntilShutdown()
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
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                println("Shutting down gRPC server...")
                this@ExampleServer.stop()
                println("Server shut down.")
            }
        })
    }

    private fun stop() = server?.shutdown()

    /** Await termination on the main thread since the grpc library uses daemon threads. */
    @Throws(InterruptedException::class)
    private fun blockUntilShutdown() = server?.awaitTermination()

    /** HelloService implementation */
    private class HelloServiceImpl : HelloServiceGrpc.HelloServiceImplBase() {

        override fun hiThere(request: HiRequest, responseObserver: StreamObserver<HiResponse>) {
            val reply = HiResponse.newBuilder().apply {
                result = """
                |Hi there! You queried: '${request.query}'
                |
                |  with tags: '${request.tagsList.joinToString(", ")}'
                |  and flags: '${request.flagsMap.map { "${it.key} = ${it.value}" }.joinToString(", ")}'
                """.trimMargin()
            }.build()
            responseObserver.onNext(reply)
            responseObserver.onCompleted()
        }
    }
}
