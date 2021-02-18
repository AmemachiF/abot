package com.amemachif.abot.server

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: ConfigManager.INSTANCE.config.port
    val server = BotServer(port)
    server.start()
    server.blockUntilShutdown()
}
