package com.amemachif.abot.server.services

import com.amemachif.abot.proto.Common
import com.amemachif.abot.proto.ReceiveMessageServiceGrpcKt
import com.amemachif.abot.proto.ServiceReceiveMessage
import com.amemachif.abot.proto.Session
import com.amemachif.abot.server.*
import com.amemachif.abot.server.exceptions.TargetNotFoundException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.job
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.toMessageChain
import kotlin.reflect.KClass

class ReceiveMessageService : ReceiveMessageServiceGrpcKt.ReceiveMessageServiceCoroutineImplBase() {
    override suspend fun countMessage(request: ServiceReceiveMessage.CountMessageRequest): ServiceReceiveMessage.CountMessageResponse {
        return botCatching {
            ServiceReceiveMessage.CountMessageResponse.newBuilder()
                .apply {
                    data = request.sessionKey.ensureBind().messageQueue.size
                }
        }
    }

    override suspend fun messageFromId(request: ServiceReceiveMessage.MessageFromIdRequest): ServiceReceiveMessage.MessageFromIdResponse {
        return botCatching {
            val message = request.sessionKey.ensureBind().cacheQueue[request.id]
            ServiceReceiveMessage.MessageFromIdResponse.newBuilder()
                .apply {
                    with(dataBuilder) {
                        when (message) {
                            is OnlineMessageSource.Outgoing.ToFriend -> friendMessageBuilder.apply {
                                senderBuilder.applyBy(message.sender.asFriend)
                            }.messageChainBuilder
                            is OnlineMessageSource.Outgoing.ToGroup -> groupMessageBuilder.apply {
                                senderBuilder.applyBy(message.target.botAsMember)
                            }.messageChainBuilder
                            is OnlineMessageSource.Outgoing.ToTemp -> tempMessageBuilder.apply {
                                senderBuilder.applyBy(message.target)
                            }.messageChainBuilder
                            is OnlineMessageSource.Incoming.FromFriend -> friendMessageBuilder.apply {
                                senderBuilder.applyBy(message.sender)
                            }.messageChainBuilder
                            is OnlineMessageSource.Incoming.FromGroup -> groupMessageBuilder.apply {
                                senderBuilder.applyBy(message.sender)
                            }.messageChainBuilder
                            is OnlineMessageSource.Incoming.FromTemp -> tempMessageBuilder.apply {
                                senderBuilder.applyBy(message.sender)
                            }.messageChainBuilder
                            else -> throw TargetNotFoundException()
                        }.addAllChain(arrayOf(message, message.originalMessage).toMessageChain().toMessageChainList())
                    }
                }
        }
    }

    override suspend fun recall(request: ServiceReceiveMessage.RecallRequest): Common.CommonEmptyResponse {
        return botCatching {
            val message = request.sessionKey.ensureBind().cacheQueue[request.target]
            message.recall()
            Common.CommonEmptyResponse.newBuilder()
        }
    }

    override suspend fun fetchMessage(request: ServiceReceiveMessage.FetchMessageRequest): ServiceReceiveMessage.FetchMessageResponse {
        return botCatching {
            val queue = request.sessionKey.ensureBind().messageQueue
            val messages = when (request.peek) {
                false -> queue::fetch
                true -> queue::peek
            }.invoke(request.count, request.latest)

            ServiceReceiveMessage.FetchMessageResponse.newBuilder()
                .apply {
                    dataList.addAll(messages)
                }
        }
    }

    override fun allStream(request: ServiceReceiveMessage.MessageEventStreamRequest): Flow<ServiceReceiveMessage.MessageEventStreamResponse> {
        return forStream(request.sessionKey, false, BotEvent::class)
    }

    override fun eventStream(request: ServiceReceiveMessage.MessageEventStreamRequest): Flow<ServiceReceiveMessage.MessageEventStreamResponse> {
        return forStream(request.sessionKey, true, MessageEvent::class)
    }

    override fun messageStream(request: ServiceReceiveMessage.MessageEventStreamRequest): Flow<ServiceReceiveMessage.MessageEventStreamResponse> {
        return forStream(request.sessionKey, false, MessageEvent::class)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T : BotEvent> forStream(
        sessionKey: Session.SessionKey,
        isExclude: Boolean,
        vararg events: KClass<T>
    ): Flow<ServiceReceiveMessage.MessageEventStreamResponse> {
        return channelFlow {
            botCatching({
                val bot = sessionKey.ensureBot()
                val listener = bot.eventChannel.subscribeAlways<BotEvent> {
                    if (this.bot === bot && (if (isExclude) events::allNot else events::any).invoke { it.isInstance(this) }) {
                        channel.send(ServiceReceiveMessage.MessageEventStreamResponse.newBuilder().apply {
                            data = this@subscribeAlways.toEventInfo()
                        }.setCode(ErrorCode.OK.code, ErrorCode.OK.message).build())
                    }
                }
                currentCoroutineContext().job.invokeOnCompletion {
                    listener.complete()
                }
                currentCoroutineContext().job.join()
            }, { e ->
                channel.send(
                    ServiceReceiveMessage.MessageEventStreamResponse.newBuilder()
                        .setCode(e.error.code, e.message)
                        .build()
                )
            })
        }
    }
}
