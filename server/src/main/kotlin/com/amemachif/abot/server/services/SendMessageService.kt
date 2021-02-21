package com.amemachif.abot.server.services

import com.amemachif.abot.proto.SendMessageServiceGrpcKt
import com.amemachif.abot.proto.ServiceSendMessage
import com.amemachif.abot.server.*
import com.amemachif.abot.server.exceptions.TargetNotFoundException
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.net.URL

class SendMessageService : SendMessageServiceGrpcKt.SendMessageServiceCoroutineImplBase() {
    override suspend fun sendFriendMessage(request: ServiceSendMessage.SendFriendMessageRequest): ServiceSendMessage.SendMessageResponse {
        return botCatching {
            val friend = request.sessionKey.ensureBot().ensureFriend(
                when (request.targetQqCase) {
                    ServiceSendMessage.SendFriendMessageRequest.TargetQqCase.TARGET -> request.target
                    ServiceSendMessage.SendFriendMessageRequest.TargetQqCase.QQ -> request.qq
                    else -> throw TargetNotFoundException()
                }
            )
            friend.sendMessage(
                request.sessionKey,
                if (request.hasQuote()) request.quote.value else null,
                request.messageChain
            )
        }
    }

    override suspend fun sendGroupMessage(request: ServiceSendMessage.SendGroupMessageRequest): ServiceSendMessage.SendMessageResponse {
        return botCatching {
            val group = request.sessionKey.ensureBot().ensureGroup(
                when (request.targetGroupCase) {
                    ServiceSendMessage.SendGroupMessageRequest.TargetGroupCase.TARGET -> request.target
                    ServiceSendMessage.SendGroupMessageRequest.TargetGroupCase.GROUP -> request.group
                    else -> throw TargetNotFoundException()
                }
            )
            group.sendMessage(
                request.sessionKey,
                if (request.hasQuote()) request.quote.value else null,
                request.messageChain
            )
        }
    }

    override suspend fun sendTempMessage(request: ServiceSendMessage.SendTempMessageRequest): ServiceSendMessage.SendMessageResponse {
        return botCatching {
            val member = request.sessionKey.ensureBot().ensureGroup(request.group).ensureMember(request.qq)
            member.sendMessage(
                request.sessionKey,
                if (request.hasQuote()) request.quote.value else null,
                request.messageChain
            )
        }
    }

    override suspend fun sendImageMessage(request: ServiceSendMessage.SendImageMessageRequest): ServiceSendMessage.SendImageMessageResponse {
        return botCatching {
            val contact = request.sessionKey.ensureBot().let {
                with(request) {
                    when (targetQqGroupCase) {
                        ServiceSendMessage.SendImageMessageRequest.TargetQqGroupCase.TARGET ->
                            it.getFriend(target) ?: it.getGroup(target)
                        ServiceSendMessage.SendImageMessageRequest.TargetQqGroupCase.QQ ->
                            it.getFriend(qq)
                        ServiceSendMessage.SendImageMessageRequest.TargetQqGroupCase.GROUP ->
                            it.getGroup(group)
                        else -> null
                    } ?: throw TargetNotFoundException()
                }
            }

            val images = request.urlsList.map { url -> URL(url).openStream().use { it.uploadAsImage(contact) } }
            val receipt = contact.sendMessage(buildMessageChain { addAll(images) })
            request.sessionKey.pushCache(receipt)
            ServiceSendMessage.SendImageMessageResponse.newBuilder()
                .apply {
                    addAllImageId(images.map { it.imageId })
                }
        }
    }
}
