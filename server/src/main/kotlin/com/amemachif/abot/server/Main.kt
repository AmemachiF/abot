package com.amemachif.abot.server

import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration
import java.util.*

suspend fun main() {
    prepareBot()
    val port = System.getenv("PORT")?.toInt() ?: ConfigManager.INSTANCE.config.port
    val server = BotServer(port)
    server.start()
    server.blockUntilShutdown()
}

suspend fun prepareBot() {
    ConfigManager.INSTANCE.config.botLogin.forEach {
        try {
            val qq = it.qq ?: return
            val password = it.password ?: return
            val config = BotConfiguration {
                fileBasedDeviceInfo()
            }
            val bot = when (it.passwordType) {
                ConfigManager.Companion.Config.BotLogin.PasswordType.PLAIN -> {
                    BotFactory.newBot(qq, password, config)
                }
                ConfigManager.Companion.Config.BotLogin.PasswordType.MD5 -> {
                    val md5 = Base64.getDecoder().decode(it.password)
                    BotFactory.newBot(qq, md5, config)
                }
            }
            if (it.autoLogin) {
                bot.login()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
