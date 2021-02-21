package com.amemachif.abot.server

import com.amemachif.abot.server.exceptions.BotNotFoundException
import com.amemachif.abot.server.exceptions.SessionInvalidException
import com.amemachif.abot.server.exceptions.SessionNotAuthException
import com.amemachif.abot.server.exceptions.WrongAuthKeyException
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.MessageEvent
import org.apache.commons.lang3.RandomStringUtils
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.concurrent.schedule

class SessionManager private constructor() {
    private val sessions = LinkedHashMap<String, Session>()

    private val sessionWatcherTimerTask: TimerTask = Timer().schedule(0L, 5000L) {
        val curr = System.currentTimeMillis()
        sessions.forEach {
            val bind = it.value.bind
            if (bind != null && curr - bind.accessTime > 30 * 60 * 1000) {
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
        session.setBind(Session.Bind(qq))
    }

    fun findSession(sessionKey: String): Session? {
        return sessions[sessionKey]
    }

    fun unbind(sessionKey: String) {
        val session = sessions[sessionKey] ?: throw SessionInvalidException()
        session.bind?.unbind()
        session.unsetBind()
    }

    class Session(
        val sessionKey: String,
        val createTime: Long = System.currentTimeMillis()
    ) {
        var bind: Bind? = null
            private set

        val isBind
            get() = checkBindValid()

        fun setBind(bind: Bind) {
            synchronized(this) {
                this.bind = bind.apply {
                    bind()
                }
            }
        }

        fun unsetBind() {
            synchronized(this) {
                bind?.unbind()
                bind = null
            }
        }

        fun ensuredBind(): Bind = bind?.apply {
            onAccess()
        } ?: throw SessionNotAuthException()

        private fun checkBindValid(): Boolean {
            synchronized(this) {
                val bind = bind ?: return false
                val bot = Bot.findInstance(bind.qq)
                if (bot == null) {
                    bind.unbind()
                    this.bind = null
                    return false
                }

                return true
            }
        }

        class Bind(
            val qq: Long
        ) {
            val messageQueue = MessageQueue()
            val cacheQueue = CacheQueue()

            var cacheSize
                get() = cacheQueue.cacheSize
                set(value) {
                    cacheQueue.cacheSize = value
                }

            private lateinit var cacheListener: Listener<MessageEvent>

            val bindTime = System.currentTimeMillis()
            var accessTime = System.currentTimeMillis()
                private set

            val bot: Bot
                get() {
                    return Bot.getInstanceOrNull(qq) ?: throw BotNotFoundException()
                }

            fun bind() {
                cacheListener = bot.eventChannel.subscribeAlways {
                    if (this.bot == this@Bind.bot) {
                        cacheQueue.add(this.source)
                    }
                }
            }

            fun unbind() {
                cacheListener.complete()
            }

            fun onAccess() {
                accessTime = System.currentTimeMillis()
            }
        }
    }

    companion object {
        val INSTANCE = SessionManager()

        fun checkSession(sessionKey: String, bindNeeded: Boolean = true): Session {
            val session = INSTANCE.findSession(sessionKey) ?: throw SessionInvalidException()
            if (bindNeeded && !session.isBind) throw SessionNotAuthException()
            return session
        }

        fun checkSession(
            sessionKey: com.amemachif.abot.proto.Session.SessionKey,
            bindNeeded: Boolean = true
        ): Session {
            return checkSession(sessionKey.sessionKey, bindNeeded)
        }
    }
}

