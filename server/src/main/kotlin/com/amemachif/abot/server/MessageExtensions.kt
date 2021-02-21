package com.amemachif.abot.server

import com.amemachif.abot.proto.MessageChainOuterClass
import com.amemachif.abot.proto.MessageChainOuterClass.MessageChainContent.MessageCase.*
import com.amemachif.abot.proto.ServiceSendMessage
import com.amemachif.abot.proto.Session
import com.amemachif.abot.server.exceptions.TargetNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.sourceIds
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import java.net.URL

suspend fun MessageChainOuterClass.MessageChain.toMiraiMessageChain(contact: Contact) =
    buildMessageChain { this@toMiraiMessageChain.chainList.forEach { it.toMiraiMessage(contact)?.let(this::add) } }

suspend fun MessageChain.toMessageChainList() =
    mutableListOf<MessageChainOuterClass.MessageChainContent>().apply {
        this@toMessageChainList.forEach { it.toMessageChainContent()?.let(this::add) }
    }

@OptIn(MiraiInternalApi::class, MiraiExperimentalApi::class)
suspend fun MessageChainOuterClass.MessageChainContent.toMiraiMessage(contact: Contact): Message? {
    return when (messageCase) {
        SOURCEMESSAGE -> null
        QUOTEMESSAGE -> null
        ATMESSAGE -> when (contact) {
            is Group -> contact.getOrFail(atMessage.target).at()
            else -> throw TargetNotFoundException()
        }
        ATALLMESSAGE -> {
            AtAll
        }
        FACEMESSAGE -> with(faceMessage) {
            when {
                hasFaceId() -> Face(faceId.value)
                hasName() -> Face(FaceMap[name.value])
                else -> Face(255)
            }
        }
        PLAINMESSAGE -> PlainText(plainMessage.text)
        IMAGEMESSAGE -> with(imageMessage) {
            when {
                hasImageId() -> Image(imageId.value)
                hasUrl() -> withContext(Dispatchers.IO) {
                    kotlin.runCatching {
                        URL(url.value).openStream().use { it.uploadAsImage(contact) }
                    }.getOrNull()
                }
                hasPath() -> TODO()
                else -> null
            }
        }
        FLASHIMAGEMESSAGE -> with(flashImageMessage) {
            when {
                hasImageId() -> Image(imageId.value)
                hasUrl() -> withContext(Dispatchers.IO) {
                    kotlin.runCatching {
                        URL(url.value).openStream().use { it.uploadAsImage(contact) }
                    }.getOrNull()
                }
                hasPath() -> TODO()
                else -> null
            }
        }?.flash()
        VOICEMESSAGE -> with(voiceMessage) {
            when {
                contact !is Group -> null
                hasVoiceId() -> Voice(voiceId.value, voiceId.value.substringBefore(".").toHexArray(), 0, 0, "")
                hasUrl() -> withContext(Dispatchers.IO) {
                    kotlin.runCatching {
                        URL(url.value).openStream().uploadAsImage(contact)
                    }.getOrNull()
                }
                hasPath() -> TODO()
                else -> null
            }
        }
        XMLMESSAGE -> SimpleServiceMessage(60, xmlMessage.xml)
        JSONMESSAGE -> SimpleServiceMessage(1, jsonMessage.json)
        APPMESSAGE -> LightApp(appMessage.content)
        POKEMESSAGE -> PokeMap[pokeMessage.name]
        MESSAGE_NOT_SET -> null
        else -> null
    }
}

@OptIn(MiraiExperimentalApi::class)
suspend fun Message.toMessageChainContent(): MessageChainOuterClass.MessageChainContent? {
    return MessageChainOuterClass.MessageChainContent.newBuilder().apply {
        when (this@toMessageChainContent) {
            is MessageSource -> sourceMessageBuilder.apply {
                id = ids.firstOrNull() ?: 0
            }
            is QuoteReply -> quoteMessageBuilder.apply {
                id = source.ids.firstOrNull() ?: 0
                groupId = when {
                    source is OfflineMessageSource && source.kind == MessageSourceKind.GROUP ||
                            source is OnlineMessageSource && (source as OnlineMessageSource).subject is Group -> source.targetId
                    else -> 0L
                }
                senderId = source.fromId
                targetId = source.targetId
                originBuilder.addAllChain((source.originalMessage + source).toMessageChainList())
            }
            is At -> atMessageBuilder.apply {
                target = this@toMessageChainContent.target
            }
            is AtAll -> atAllMessageBuilder
            is Face -> faceMessageBuilder.apply {
                faceIdBuilder.value = this@toMessageChainContent.id
                nameBuilder.value = this@toMessageChainContent.name
            }
            is PlainText -> plainMessageBuilder.apply {
                text = this@toMessageChainContent.content
            }
            is Image -> imageMessageBuilder.apply {
                imageIdBuilder.value = this@toMessageChainContent.imageId
                urlBuilder.value = this@toMessageChainContent.queryUrl()
            }
            is FlashImage -> flashImageMessageBuilder.apply {
                imageIdBuilder.value = this@toMessageChainContent.image.imageId
                urlBuilder.value = this@toMessageChainContent.image.queryUrl()
            }
            is Voice -> voiceMessageBuilder.apply {
                voiceIdBuilder.value = this@toMessageChainContent.fileName
                urlBuilder.value = this@toMessageChainContent.url
            }
            is ServiceMessage -> xmlMessageBuilder.apply {
                xml = this@toMessageChainContent.content
            }
            is LightApp -> appMessageBuilder.apply {
                content = this@toMessageChainContent.content
            }
            is PokeMessage -> pokeMessageBuilder.apply {
                name = this@toMessageChainContent.name
            }
            else -> return null
        }
    }.build()
}

fun Message.withQuote(quote: QuoteReply?): Message {
    return if (quote == null)
        this
    else {
        (quote + this).asIterable().toMessageChain()
    }
}

suspend fun Contact.sendMessage(
    sessionKey: Session.SessionKey,
    quoteId: Int?,
    messageChain: MessageChainOuterClass.MessageChain
): ServiceSendMessage.SendMessageResponse.Builder {
    val quote = sessionKey.getCache(quoteId)?.quote()
    val receipt = this.sendMessage(messageChain.toMiraiMessageChain(this).withQuote(quote))
    sessionKey.pushCache(receipt)
    return ServiceSendMessage.SendMessageResponse.newBuilder()
        .apply {
            messageId = receipt.sourceIds.firstOrNull() ?: 0
        }
}

