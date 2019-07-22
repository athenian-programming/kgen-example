package org.athenian

import io.grpc.stub.StreamObserver

class HelloServiceImpl : HelloServiceGrpc.HelloServiceImplBase() {

    override fun hiThere(request: HiRequest, responseObserver: StreamObserver<HiResponse>) {
        val reply = HiResponse.newBuilder().apply {
            result = """
                |Hi there! You queried: '${request.query}'
                |
                |  with tags: '${request.tagsList.joinToString(", ")}'
                |  and flags: '${request.flagsMap.map { "${it.key} = ${it.value}" }.joinToString(", ")}'
                """.trimMargin()
        }.build()

        responseObserver.apply {
            onNext(reply)
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
                val msg =
                    HiResponse.newBuilder()
                        .apply { result = "Hello ${names.joinToString(", ")}" }
                        .build()
                responseObserver
                    ?.apply {
                        onNext(msg)
                        onCompleted()
                    }
            }
        }

    override fun hiThereWithManyReponses(request: HiRequest?, responseObserver: StreamObserver<HiResponse>?) {
        repeat(5) {
            val reply =
                HiResponse.newBuilder()
                    .apply { result = "Hello ${request?.query ?: "Missing"} [$it]" }
                    .build()
            responseObserver?.onNext(reply)
        }
        responseObserver?.onCompleted()
    }

    override fun hiThereWithManyRequestsAndManyReponses(responseObserver: StreamObserver<HiResponse>?): StreamObserver<HiRequest> {
        repeat(5) {
            val reply =
                HiResponse.newBuilder()
                    .apply { result = "Hello there [$it]" }
                    .build()
            responseObserver?.onNext(reply)
        }
        responseObserver?.onCompleted()

        return object : StreamObserver<HiRequest> {
            val names: MutableList<String> = mutableListOf()

            override fun onNext(request: HiRequest) {
                names.add(request.query)
            }

            override fun onError(t: Throwable) {
                println("Encountered error in hiThereWithManyRequestsAndManyReponses()")
                t.printStackTrace()
            }

            override fun onCompleted() {
                val msg =
                    HiResponse.newBuilder()
                        .apply { result = "Hello ${names.joinToString(", ")}" }
                        .build()
                responseObserver
                    ?.apply {
                        onNext(msg)
                        onCompleted()
                    }
            }
        }
    }
}
