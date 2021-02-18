package com.amemachif.abot.server.services

import com.amemachif.abot.proto.BotManageServiceGrpcKt
import com.amemachif.abot.proto.Common
import com.amemachif.abot.proto.ServiceBotManage
import com.amemachif.abot.server.BotExtensions.botCatching
import com.amemachif.abot.server.exceptions.BotNotFoundException
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration

class BotManageService : BotManageServiceGrpcKt.BotManageServiceCoroutineImplBase() {
    override suspend fun newBot(request: ServiceBotManage.NewBotRequest): Common.CommonResponse {
        return botCatching {
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
}
