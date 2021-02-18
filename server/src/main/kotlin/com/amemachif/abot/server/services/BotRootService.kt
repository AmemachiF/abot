package com.amemachif.abot.server.services

import com.amemachif.abot.proto.BotRootServiceGrpcKt
import com.amemachif.abot.proto.Common
import com.amemachif.abot.proto.ServiceBotRoot
import com.amemachif.abot.server.SessionManager
import com.amemachif.abot.server.botCatching
import com.google.protobuf.Empty
import java.util.*

class BotRootService : BotRootServiceGrpcKt.BotRootServiceCoroutineImplBase() {
    override suspend fun about(request: Empty): ServiceBotRoot.AboutResponse {
        return botCatching {
            val versionStream = this.javaClass.classLoader.getResourceAsStream("version.properties")
            val version = if (versionStream == null) {
                "UNKNOWN"
            } else {
                val properties = Properties()
                kotlin.runCatching {
                    properties.load(versionStream)
                }
                properties.getProperty("version", "UNKNOWN")
            }
            ServiceBotRoot.AboutResponse.newBuilder()
                .apply {
                    dataBuilder.version = version ?: "UNKNOWN"
                }
        }
    }

    override suspend fun auth(request: ServiceBotRoot.AuthRequest): ServiceBotRoot.AuthResponse {
        return botCatching {
            val sessionKey = SessionManager.INSTANCE.newSession(request.authKey)
            ServiceBotRoot.AuthResponse.newBuilder()
                .apply {
                    session = sessionKey
                }
        }
    }

    override suspend fun verify(request: ServiceBotRoot.VerifyReleaseRequest): Common.CommonResponse {
        return botCatching {
            SessionManager.INSTANCE.bind(request.sessionKey.sessionKey, request.qq)
            Common.CommonResponse.newBuilder()
        }
    }

    override suspend fun release(request: ServiceBotRoot.VerifyReleaseRequest): Common.CommonResponse {
        return botCatching {
            SessionManager.INSTANCE.unbind(request.sessionKey.sessionKey)
            Common.CommonResponse.newBuilder()
        }
    }
}
