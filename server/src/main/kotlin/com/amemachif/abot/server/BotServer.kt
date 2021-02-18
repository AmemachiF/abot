package com.amemachif.abot.server

import com.amemachif.abot.server.services.BotManageService
import com.amemachif.abot.server.services.BotRootService
import com.amemachif.abot.server.services.ListService
import io.grpc.Server
import io.grpc.ServerBuilder

class BotServer(private val port: Int) {
    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(BotManageService())
        .addService(BotRootService())
        .addService(ListService())
        .build()

    fun start() {
        server.start()
        println("Bot server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down bot server since JVM is shutting down")
                this@BotServer.stop()
                println("*** server shut down")
            }
        )
    }

    fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}
