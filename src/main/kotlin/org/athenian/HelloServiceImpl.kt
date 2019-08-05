package org.athenian

import io.grpc.stub.StreamObserver

class HelloServiceImpl : HelloServiceGrpc.HelloServiceImplBase() {

    override fun hiThere(request: HiRequest, responseObserver: StreamObserver<HiResponse>) {
        responseObserver
            .apply {
                val arg = """
                    Hello! You queried: '${request.query}'
                        with tags: '${request.tagsList.joinToString(", ")}'
                        and flags: '${request.flagsMap.map { "${it.key} = ${it.value}" }.joinToString(", ")}'
                    """.trimIndent()
                val response = HiResponse.newBuilder().apply { result = arg }.build()
                onNext(response)
                onCompleted()
            }
    }

    override fun hiThereWithManyRequests(responseObserver: StreamObserver<HiResponse>?) =
        object : StreamObserver<HiRequest> {
            val names: MutableList<String> = mutableListOf()

            override fun onNext(request: HiRequest) {
                names.add(request.query)
            }

            override fun onError(t: Throwable) {
                println("Encountered error in hiThereWithManyRequests()")
                t.printStackTrace()
            }

            override fun onCompleted() {
                responseObserver
                    ?.apply {
                        val arg = "Hello ${names.joinToString(", ")}"
                        val response = HiResponse.newBuilder().apply { result = arg }.build()
                        onNext(response)
                        onCompleted()
                    }
            }
        }

    override fun hiThereWithManyResponses(request: HiRequest?, responseObserver: StreamObserver<HiResponse>?) {
        repeat(5) {
            val arg = "Hello ${request?.query ?: "Missing"} [$it]"
            val response = HiResponse.newBuilder().apply { result = arg }.build()
            responseObserver?.onNext(response)
        }
        responseObserver?.onCompleted()
    }

    override fun hiThereWithManyRequestsAndManyResponses(responseObserver: StreamObserver<HiResponse>) =
        object : StreamObserver<HiRequest> {
            override fun onNext(request: HiRequest) {
                repeat(3) {
                    val arg = "Hello ${request.query} [$it]"
                    val response = HiResponse.newBuilder().apply { result = arg }.build()
                    responseObserver.onNext(response)
                }
            }

            override fun onError(t: Throwable) {
                println("Encountered error in hiThereWithManyRequestsAndManyResponses()")
                t.printStackTrace()
            }

            override fun onCompleted() {
                responseObserver.onCompleted()
            }
        }
}
