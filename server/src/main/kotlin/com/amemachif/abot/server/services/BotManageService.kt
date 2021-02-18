package com.amemachif.abot.server.services

import com.amemachif.abot.proto.BotManageServiceGrpcKt
import com.amemachif.abot.proto.Common
import com.amemachif.abot.proto.ServiceBotManage
import com.amemachif.abot.server.ConfigManager
import com.amemachif.abot.server.botCatching
import com.amemachif.abot.server.exceptions.BotNotFoundException
import com.amemachif.abot.server.exceptions.WrongAuthKeyException
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration

class BotManageService : BotManageServiceGrpcKt.BotManageServiceCoroutineImplBase() {
    override suspend fun newBot(request: ServiceBotManage.NewBotRequest): Common.CommonResponse {
        return botCatching {
            checkManageKey(request.manageKey)
            if (Bot.findInstance(request.qq) != null) {
                Common.CommonResponse.newBuilder()
            } else {
                val config = BotConfiguration {
                    if (request.hasConfig()) {
                        if (request.config.hasDeviceInfo()) loadDeviceInfoJson(request.config.deviceInfo.value)
                    } else {
                        fileBasedDeviceInfo()
                    }
                }
                val bot = when (request.passwordCase.number) {
                    ServiceBotManage.NewBotRequest.PASSWORDPLAIN_FIELD_NUMBER -> {
                        BotFactory.newBot(request.qq, request.passwordPlain, config)
                    }
                    ServiceBotManage.NewBotRequest.PASSWORDMD5_FIELD_NUMBER -> {
                        BotFactory.newBot(request.qq, request.passwordMd5.toByteArray(), config)
                    }
                    else -> throw NoSuchMethodException()
                }
                if (request.autoLogin) {
                    bot.login()
                }
                Common.CommonResponse.newBuilder()
            }
        }
    }

    override suspend fun botStatus(request: ServiceBotManage.BotStatusRequest): ServiceBotManage.BotStatusResponse {
        return botCatching {
            checkManageKey(request.manageKey)
            val bot = Bot.getInstanceOrNull(request.qq) ?: throw BotNotFoundException()
            ServiceBotManage.BotStatusResponse.newBuilder()
                .apply {
                    statusBuilder.apply {
                        qq = bot.id
                        isOnline = try {
                            bot.isOnline
                        } catch (e: UninitializedPropertyAccessException) {
                            false
                        }
                    }
                }
        }
    }

    override suspend fun botList(request: ServiceBotManage.BotListRequest): ServiceBotManage.BotListResponse {
        return botCatching {
            checkManageKey(request.manageKey)
            ServiceBotManage.BotListResponse.newBuilder()
                .apply {
                    Bot.instances.forEach {
                        addBotsBuilder().apply {
                            qq = it.id
                            try {
                                nicknameBuilder.value = it.nick
                            } catch (e: UninitializedPropertyAccessException) {
                            }

                        }
                    }
                }
        }
    }

    override suspend fun login(request: ServiceBotManage.LoginRequest): Common.CommonResponse {
        return botCatching {
            checkManageKey(request.manageKey)

            val bot = Bot.getInstanceOrNull(request.qq) ?: throw BotNotFoundException()
            bot.login()

            Common.CommonResponse.newBuilder()
        }
    }

    companion object {
        private fun checkManageKey(manageKey: String) {
            if (manageKey != ConfigManager.INSTANCE.config.manageKey) {
                throw WrongAuthKeyException()
            }
        }
    }
}
