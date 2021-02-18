package com.amemachif.abot.server

import com.amemachif.abot.server.exceptions.BotNotFoundException
import com.amemachif.abot.server.exceptions.SessionInvalidException
import com.amemachif.abot.server.exceptions.WrongAuthKeyException
import kotlin.collections.LinkedHashMap
import kotlin.concurrent.schedule
import net.mamoe.mirai.Bot
import org.apache.commons.lang3.RandomStringUtils
import java.util.*

class SessionManager private constructor() {
    private val sessions = LinkedHashMap<String, Session>()

    private val sessionWatcherTimerTask: TimerTask = Timer().schedule(0L, 5000L) {
        val curr = System.currentTimeMillis()
        sessions.forEach {
            val bind = it.value.bind
            if (bind != null && curr - bind.accessTime > 10 * 1000) {
                unbind(it.key)
            }
        }
    }

    fun newSession(authKey: String): String {
        if (authKey != ConfigManager.INSTANCE.config.authKey) {
            throw WrongAuthKeyException()
        }
        var key: String
        do {
            key = RandomStringUtils.randomAlphanumeric(30)
        } while (sessions.containsKey(key))
        sessions[key] = Session(key)

        return key
    }

    fun bind(sessionKey: String, qq: Long) {
        val session = sessions[sessionKey] ?: throw SessionInvalidException()
        session.bind = Session.Bind(qq).apply {
            bind()
        }
    }

    fun unbind(sessionKey: String) {
        val session = sessions[sessionKey] ?: throw SessionInvalidException()
        session.bind?.unbind()
        session.bind = null
    }

    companion object {
        val INSTANCE = SessionManager()
    }

    class Session(
        val sessionKey: String,
        val createTime: Long = System.currentTimeMillis()
    ) {
        var bind: Bind? = null

        class Bind(
            val qq: Long
        ) {
            var bot: Bot
                private set

            val bindTime = System.currentTimeMillis()
            var accessTime = System.currentTimeMillis()
                private set

            init {
                bot = Bot.getInstanceOrNull(qq) ?: throw BotNotFoundException()
            }

            fun bind() {
            }

            fun unbind() {
            }

            private fun onAccess() {
                accessTime = System.currentTimeMillis()
            }
        }
    }
}
